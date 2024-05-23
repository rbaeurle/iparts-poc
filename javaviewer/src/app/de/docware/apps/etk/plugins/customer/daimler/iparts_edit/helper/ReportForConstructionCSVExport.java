/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.datatypes.VisObject;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataKgTuPrediction;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataKgTuPredictionList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * Klasse für das Reporting + CSV-Export von konstruktiven Stücklisten
 */
public class ReportForConstructionCSVExport extends ReportForConstructionNodes {

    private static final String FILE_NAME_DELIMITER = "_";
    private static final String TABLE_DUMMY = "TABLE_DUMMY";
    private static final String FIELD_KG = "KG";
    private static final String FIELD_TU = "TU";
    private static final String FIELD_REMAINING_KGTU = "REMAINING_KGTU";

    private static String[] FIRST_COLUMNS_NAMES = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO),
                                                                TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_HM),
                                                                TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_M),
                                                                TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SM),
                                                                TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE),
                                                                TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV) };
    private static Set<String> EXTRA_COLUMNS_FIELD_NAMES = new LinkedHashSet<>();
    private static Set<String> DONT_SHOW_NAMES = new LinkedHashSet<>();
    private static Set<String> DB_FORMAT_NAMES = new LinkedHashSet<>();

    static {
        // Spalten, die nicht angezeigt werden sollen
        DONT_SHOW_NAMES.add(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_LFDNR));
        // Spalten im DB Format
        DB_FORMAT_NAMES.add(TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR));
        // Zusätzliche Spalten
        EXTRA_COLUMNS_FIELD_NAMES.add(FIELD_KG);
        EXTRA_COLUMNS_FIELD_NAMES.add(FIELD_TU);
        EXTRA_COLUMNS_FIELD_NAMES.add(FIELD_REMAINING_KGTU);
    }


    private CsvZipExportWriter exportWriter;
    private List<EtkDisplayField> headerFields;

    private Map<String, List<EtkDataPartListEntry>> transferredPartListEntries;
    private iPartsSeriesId seriesId;
    private Map<String, List<iPartsProduct>> aaDetectedProductMap;
    private List<iPartsProduct> preFilteredProducts;
    private Map<String, iPartsDataModuleEinPASList> einPASListMap;
    private Map<iPartsDialogBCTEPrimaryKey, KgTuId> bcteKeyToPredictedKGTUMap;
    private boolean isCompleteSeriesExport; // Flag, ob eine komplette Baureihe exportiert wird
    private String exportHeaderLang;

    private String exportLineLang;

    public ReportForConstructionCSVExport(EtkProject projectForCalculation, EtkProject projectForGUI, String exportHeaderLang,
                                          String exportLineLang) {
        super(projectForCalculation, projectForGUI);
        this.exportHeaderLang = exportHeaderLang;
        this.exportLineLang = exportLineLang;
    }

    /**
     * Report Eintrag (zum Sperren) erzeugen
     * hier können weitere Initialisierungen vorgenommen werden
     *
     * @param seriesNumber
     * @param errMsg
     * @return
     */
    @Override
    protected boolean makeAndCheckStartNode(String seriesNumber, String errMsg) {
        boolean result = super.makeAndCheckStartNode(seriesNumber, errMsg);
        if (result) {
            seriesId = new iPartsSeriesId(seriesNumber);
        }
        return result;
    }

    @Override
    protected boolean calculatePartListEntry(EtkDataPartListEntry partListEntry) {
        boolean result = super.calculatePartListEntry(partListEntry);
        if (result) {
            // nur AS-Relevante StüLis exportieren
            if (partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT)) {
                writeHeader(partListEntry);
                // KG/TU Ausgabeliste zusammenbauen
                Set<KgTuId> foundKGTUIds = collectAllKGTUInformation(partListEntry);
                // ein PartListEntry in Zip-Stream schreiben
                writePartListEntry(partListEntry, foundKGTUIds);
            }
        }
        return result;
    }

    @Override
    protected boolean checkIfHmMSmNodeValidForSubTree(HmMSmNode hmMSmNode) {
        return (hmMSmNode != null) && !hmMSmNode.isHiddenRecursively();
    }

    @Override
    protected boolean calculateSeriesHmMNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId, VarParam<Integer> openEntries, VarParam<Integer> changedEntries) {
        if (!initExportFile(hmMSmId)) {
            return false;
        }
        return super.calculateSeriesHmMNode(assembly, parentAssemblyId, hmMSmId, openEntries, changedEntries);
    }

    @Override
    protected boolean calculateSmNode(EtkDataAssembly assembly, AssemblyId parentAssemblyId, HmMSmId hmMSmId, VarParam<Integer> openEntries, VarParam<Integer> changedEntries) {
        if (!initExportFile(hmMSmId)) {
            return false;
        }
        return super.calculateSmNode(assembly, parentAssemblyId, hmMSmId, openEntries, changedEntries);
    }

    /**
     * Initialisiert die Export-Datei
     *
     * @param hmMSmId
     * @return
     */
    private boolean initExportFile(HmMSmId hmMSmId) {
        if (exportWriter != null) {
            return true;
        }
        // Wird ein gültiger HM/M/SM-Knoten übergeben, dann handelt es sich nicht um einen Export einer kompletten Baureihe
        isCompleteSeriesExport = hmMSmId == null;
        // temp Verzeichnis anlegen
        String exportFileName = makeFilename(hmMSmId);
        exportWriter = new CsvZipExportWriter();
        if (!exportWriter.open(exportFileName)) {
            exportWriter = null;
            return false;
        }
        return true;
    }

    /**
     * Erstellt den Dateinamen für die Exportdatei
     *
     * @param hmMSmId
     * @return
     */
    private String makeFilename(HmMSmId hmMSmId) {
        String fileName = "";
        if (hmMSmId != null) {
            fileName = addFileNameElement(fileName, hmMSmId.getSeries());
            fileName = addFileNameElement(fileName, hmMSmId.getHm());
            fileName = addFileNameElement(fileName, hmMSmId.getM());
            fileName = addFileNameElement(fileName, hmMSmId.getSm());
        } else if (seriesId != null) {
            fileName = addFileNameElement(fileName, seriesId.getSeriesNumber());
        }

        return fileName + lockReportConstNodeCalculationDate;
    }

    private static String addFileNameElement(String fileName, String textElement) {
        if (StrUtils.isValid(textElement)) {
            if (StrUtils.isValid(fileName)) {
                return fileName + textElement + FILE_NAME_DELIMITER;
            } else {
                return textElement + FILE_NAME_DELIMITER;
            }
        }
        return fileName;
    }

    /**
     * Liefert den Präfix für den Dateinamen zu einem Export einer kompletten Baureihe
     *
     * @param seriesId
     * @return
     */
    public static String getSeriesPrefixForFilename(iPartsSeriesId seriesId) {
        if (seriesId != null) {
            return addFileNameElement("", seriesId.getSeriesNumber());
        }
        return null;
    }

    /**
     * Sammelt alle KGTU Informationen zur übergebenen Stücklistenposition. Hierbei handelt es sich um Verortungen in
     * der AS-Stückliste, berechnete Vorschläge sowie Vorschläge via KI-Import
     *
     * @param partListEntry
     * @return
     */
    private Set<KgTuId> collectAllKGTUInformation(EtkDataPartListEntry partListEntry) {
        Set<KgTuId> kgTuIdSet = new LinkedHashSet<>();
        Set<KgTuId> kgTuIdProposalSet = new LinkedHashSet<>();
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (bcteKey != null) {
            // finde Matching Products zur Ausführungsart
            List<iPartsProduct> matchingProducts = findMatchingProducts(bcteKey.getAA());
            // finde bereits nach AS übertragene PartListEntries
            List<EtkDataPartListEntry> retailPartListEntries = getRetailPartListEntries(bcteKey);
            boolean isTransferedToAS = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(iPartsDataVirtualFieldsDefinition.RETAIL_ASSIGNED);
            if (isTransferedToAS) {
                // bereits nach AS transferiert
                // suche in Liste der AS-SourceContext die richtige DIALOG-Guid
                List<EtkDataPartListEntry> retailSourceGuidPartListEntries = findGuidInFoundRetailPartListEntries(bcteKey, retailPartListEntries);
                if (!retailSourceGuidPartListEntries.isEmpty()) {
                    calculateKgTu(kgTuIdSet, matchingProducts, retailSourceGuidPartListEntries);
                }
            } else {
                // Vorschlage bestimmen
                if (!matchingProducts.isEmpty()) {
                    // suche in Liste der AS-SourceContext die DIALOG-Guid ohne sDatA und/oder PosV
                    List<EtkDataPartListEntry> retailSourceGuidPartListEntries = findGuidInFoundRetailPartListEntriesProposal(bcteKey, retailPartListEntries);
                    if (!retailSourceGuidPartListEntries.isEmpty()) {
                        calculateKgTu(kgTuIdProposalSet, matchingProducts, retailSourceGuidPartListEntries);
                    }
                }
                // zum Schluß noch ggf. den KI-Vorschlag hinzufügen
                addPredictedKgTu(bcteKey, kgTuIdProposalSet);
            }
        }
        return kgTuIdSet.isEmpty() ? kgTuIdProposalSet : kgTuIdSet;
    }

    private List<EtkDataPartListEntry> getRetailPartListEntries(iPartsDialogBCTEPrimaryKey bcteKey) {
        String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, bcteKey.getHmMSmId());
        List<EtkDataPartListEntry> retailPartListEntries = transferredPartListEntries.get(sourceContext);
        if (retailPartListEntries == null) {
            // neue StüLi => nicht zuviel speichern
            transferredPartListEntries.clear();
            // damit auch die KI-KG/TUs angepasst werden
            bcteKeyToPredictedKGTUMap = null;
            retailPartListEntries = EditConstructionToRetailHelper.getRetailPartListEntries(iPartsEntrySourceType.DIALOG, sourceContext,
                                                                                            null, projectForCalculation);
            transferredPartListEntries.put(sourceContext, retailPartListEntries);
        }
        return retailPartListEntries;
    }

    private void calculateKgTu(Set<KgTuId> kgTuIdSet, List<iPartsProduct> matchingProducts, List<EtkDataPartListEntry> retailSourceGuidPartListEntries) {
        if (!matchingProducts.isEmpty()) {
            for (iPartsProduct product : matchingProducts) {
                for (EtkDataPartListEntry retailPartlistEntry : retailSourceGuidPartListEntries /*retailPartListEntries*/) {
                    String assemblyNr = retailPartlistEntry.getAsId().getKVari();
                    iPartsDataModuleEinPASList einPASList = einPASListMap.get(assemblyNr);
                    if (einPASList == null) {
                        einPASList = iPartsDataModuleEinPASList.loadForModule(projectForCalculation, new AssemblyId(assemblyNr, ""));
                        einPASListMap.put(assemblyNr, einPASList);
                    }
                    for (iPartsDataModuleEinPAS einPasModuleItem : einPASList) {
                        String productNumber = einPasModuleItem.getAsId().getProductNumber();
                        if (productNumber.equals(product.getAsId().getProductNumber())) {
                            // KG/TU Verortung eintragen
                            KgTuId kgTuId = new KgTuId(einPasModuleItem.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                                       einPasModuleItem.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU));
                            kgTuIdSet.add(kgTuId);
                        }
                    }
                }
            }
        }
    }

    private void addPredictedKgTu(iPartsDialogBCTEPrimaryKey bcteKey, Set<KgTuId> kgTuIdProposalList) {
        if (kgTuIdProposalList.isEmpty()) {
            // keine Entsprechung in der AS-SourceContext-Liste gefunden => mach Vorhersage
            KgTuId kgTuId = getPredictedKGTUforBCTEKey(bcteKey);
            if (kgTuId != null) {
                kgTuIdProposalList.add(kgTuId);
            }
        }
        // Alternative: KI-KG/TUs speziell markieren
//        // keine Entsprechung in der AS-SourceContext-Liste gefunden => mach Vorhersage
//        KgTuId kgTuId = getPredictedKGTUforBCTEKey(bcteKey);
//        if ((kgTuId != null) && !kgTuIdProposalList.contains(kgTuId)) {
//            kgTuIdProposalList.add(new KgTuId("+" + kgTuId.getKg(), kgTuId.getTu()));
//        }
    }

    public KgTuId getPredictedKGTUforBCTEKey(iPartsDialogBCTEPrimaryKey requestedBCTEKey) {
        if (bcteKeyToPredictedKGTUMap == null) {
            bcteKeyToPredictedKGTUMap = new HashMap<>();
            String dialogSourceContext = requestedBCTEKey.getHmMSmId().getDIALOGSourceContext();
            iPartsDataKgTuPredictionList kgTuPredictions = iPartsDataKgTuPredictionList.loadListForHmMSmWithSeries(projectForCalculation, dialogSourceContext);
            for (iPartsDataKgTuPrediction kgTuPrediction : kgTuPredictions) {
                String bcteKeyString = kgTuPrediction.getAsId().getDialogId();
                iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKeyString);
                bcteKeyToPredictedKGTUMap.put(bcteKey, kgTuPrediction.getKgTuId());
            }
        }
        return bcteKeyToPredictedKGTUMap.get(requestedBCTEKey);
    }

    private List<EtkDataPartListEntry> findGuidInFoundRetailPartListEntries(iPartsDialogBCTEPrimaryKey bcteKey,
                                                                            List<EtkDataPartListEntry> foundRetailPartListEntries) {
        List<EtkDataPartListEntry> result = new DwList<>();
        if (bcteKey == null) {
            return result;
        }
        String guid = bcteKey.createDialogGUID();
        for (EtkDataPartListEntry partListEntry : foundRetailPartListEntries) {
            iPartsDialogBCTEPrimaryKey compbcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            if (compbcteKey == null) {
                continue;
            }
            if (guid.equals(compbcteKey.createDialogGUID())) {
                result.add(partListEntry);
            }
        }
        return result;
    }

    private List<EtkDataPartListEntry> findGuidInFoundRetailPartListEntriesProposal(iPartsDialogBCTEPrimaryKey bcteKey,
                                                                                    List<EtkDataPartListEntry> foundRetailPartListEntries) {
        List<EtkDataPartListEntry> result = new DwList<>();
        if (bcteKey == null) {
            return result;
        }
        iPartsDialogBCTEPrimaryKey bcteKeyOne = bcteKey.getPositionBCTEPrimaryKeyWithoutSDA();
        iPartsDialogBCTEPrimaryKey bcteKeyTwo = new iPartsDialogBCTEPrimaryKey(bcteKey.seriesNo, bcteKey.hm, bcteKey.m, bcteKey.sm,
                                                                               bcteKey.posE, "", bcteKey.ww, bcteKey.et,
                                                                               bcteKey.aa, "");

        String guidOne = bcteKeyOne.createDialogGUID();
        String guidTwo = bcteKeyTwo.createDialogGUID();
        for (EtkDataPartListEntry partListEntry : foundRetailPartListEntries) {
            iPartsDialogBCTEPrimaryKey compBcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            if (compBcteKey == null) {
                continue;
            }
            bcteKeyOne = compBcteKey.getPositionBCTEPrimaryKeyWithoutSDA();
            bcteKeyTwo = new iPartsDialogBCTEPrimaryKey(compBcteKey.seriesNo, compBcteKey.hm, compBcteKey.m, compBcteKey.sm,
                                                        compBcteKey.posE, "", compBcteKey.ww, compBcteKey.et,
                                                        compBcteKey.aa, "");
            if (guidOne.equals(bcteKeyOne.createDialogGUID()) || guidTwo.equals(bcteKeyTwo.createDialogGUID())) {
                result.add(partListEntry);
            }
        }
        return result;
    }

    protected List<iPartsProduct> preFilterProducts() {
        if (preFilteredProducts == null) {
            // Liste aller Produkte und Eingrenzen nach referenzierter Baureihe (früher wurde nach der verknüpften Baureihe eingegrenzt)
            List<iPartsProduct> productList = iPartsProduct.getAllProducts(projectForCalculation);
            preFilteredProducts = new DwList<>();
            for (iPartsProduct product : productList) {
                if ((product.getReferencedSeries() != null) && product.getReferencedSeries().equals(seriesId)) {
                    iPartsModuleTypes moduleType = product.getDocumentationType().getModuleType(false);
                    // Damit für die Übernahme aus der DIALOG-Konstruktion keine Nicht-DIALOG-Produkte verwendet werden,
                    // werden die Suchergebnisse bezüglich des Modultyps überprüft
                    // Wenn das Produkt keinen Dokumentationstyp hat, dann wird der Modultyp später anahnd der Herkunft bestimmt (HmMSm oder OPSSaa)
                    if (iPartsModuleTypes.isDialogRetailType(moduleType) || (product.getDocumentationType() == iPartsDocumentationType.UNKNOWN)) {
                        // nur KG/TU-Produkte erlaubt
                        if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                            preFilteredProducts.add(product);
                        }
                    }
                }
            }
        }
        return preFilteredProducts;
    }

    protected List<iPartsProduct> findMatchingProducts(String matrixCode) {
        List<iPartsProduct> resultProducts = aaDetectedProductMap.get(matrixCode);
        if (resultProducts == null) {
            resultProducts = new DwList<>();
            for (iPartsProduct product : preFilterProducts()) {
                if (product.hasModelsWithAA(projectForCalculation, matrixCode)) {
                    resultProducts.add(product);
                }
            }
            aaDetectedProductMap.put(matrixCode, resultProducts);
        }

        return resultProducts;
    }


    /**
     * wird immer im finnaly durchlaufen => hier kann aufgräumt werden
     *
     * @param wasCancelled
     * @param hasException
     * @return
     */
    @Override
    protected boolean removeLockForReport(boolean wasCancelled, boolean hasException) {
        boolean result = super.removeLockForReport(wasCancelled, hasException);
        if (exportWriter != null) {
            // Zip-Stream schließen
            exportWriter.closeOutputStreams();
            if (wasCancelled || hasException) {
                exportWriter.clearAfterDownload();
            }
        }
        return result;
    }

    @Override
    protected void doPostCalcOperations(boolean withGuiActions) {
        if (exportWriter != null) {
            // Datei nur in das vorgegebene Verzeichnis verschieben, wenn eine komplette Baureihe exportiert wurde
            if (isCompleteSeriesExport) {
                DWFile destDirForExport = iPartsEditPlugin.getDirForAutoCalcAndExport();
                if ((destDirForExport != null) && destDirForExport.exists(1000)) {
                    // Bestehende Exporte zur aktuellen Baureihe löschen
                    deleteExistingExports();
                    DWFile exportFile = exportWriter.getExportFile();
                    if ((exportFile != null) && exportFile.exists(1000)) {
                        if (!exportFile.copy(destDirForExport, true)) {
                            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR,
                                       "Could not copy export file \"" + exportFile.getAbsolutePath()
                                       + "\" to export directory \"" + destDirForExport.getAbsolutePath() + "\"");

                        }
                    }
                }
            }

            // Datei zum Upload anbieten
            if (withGuiActions) {
                exportWriter.downloadExportFile();
            }
        }
    }

    /**
     * Löscht im eingestellten Exportverzeichnis alle Exporte zur aktuellen Baureihe
     */
    private void deleteExistingExports() {
        if (seriesId != null) {
            List<DWFile> filesForSeries = getExportFilesForSeries(seriesId);
            if (!filesForSeries.isEmpty()) {
                for (DWFile existingExportFile : filesForSeries) {
                    if (!existingExportFile.deleteRecursivelyWithRepeat(5)) {
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR,
                                   "Could not delete existing export file \"" + existingExportFile.getAbsolutePath() + "\"");
                    }
                }
            }
        }
    }

    /**
     * Liefert alle bisher exportierten Dateien zur übergebenen Baureihe sortiert nach dem Zeitstempel im Namen.
     *
     * @param seriesId
     * @return
     */
    public static List<DWFile> getExportFilesForSeries(final iPartsSeriesId seriesId) {
        DWFile destDirForExport = iPartsEditPlugin.getDirForAutoCalcAndExport();
        List<DWFile> result = new ArrayList<>();
        if ((destDirForExport != null) && destDirForExport.exists(1000) && !destDirForExport.isEmpty()) {
            result.addAll(destDirForExport.listDWFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // Präfix genauso bestimmen, wie beim erzeugen der Datei
                    String prefix = getSeriesPrefixForFilename(seriesId);
                    return pathname.getName().startsWith(prefix);
                }
            }));

            // Sollten mehrere Dateien existieren -> Sortieren nach Datum im Dateinamen
            if (result.size() > 1) {
                Collections.sort(result, new Comparator<DWFile>() {
                    @Override
                    public int compare(DWFile o1, DWFile o2) {
                        String filename1 = o1.extractFileName(false);
                        String filename2 = o2.extractFileName(false);
                        return filename2.compareTo(filename1);
                    }
                });
            }
        }
        return result;
    }

    @Override
    protected void clearAfterCalcFinished() {
        if (exportWriter != null) {
            exportWriter.clearAfterDownload();
        }
    }

    private void writeHeader(EtkDataPartListEntry partListEntry) {
        // Check, on die Spalten schon bestimmt wurden
        if (headerFields != null) {
            return;
        }
        headerFields = new DwList<>();
        List<String> header = new DwList<>();
        if (partListEntry == null) {
            return;
        }
        List<EtkDisplayField> displayFieldList = partListEntry.getOwnerAssembly().getEbene().getVisibleFields();

        // zuerst die Reihenfolge der ersten Spalten festlegen
        List<String> dataBaseFallbackLanguages = projectForCalculation.getDataBaseFallbackLanguages();
        for (String tableAndFieldName : FIRST_COLUMNS_NAMES) {
            EtkDisplayField displayField = findDisplayField(tableAndFieldName, displayFieldList);
            if (displayField == null) {
                if (!DONT_SHOW_NAMES.contains(tableAndFieldName)) {
                    displayField = new EtkDisplayField(tableAndFieldName, false, false);
                    // Text noch setzen
                    EtkDatabaseField dbField = projectForCalculation.getConfig().getFieldDescription(TableAndFieldName.getTableName(tableAndFieldName),
                                                                                                     TableAndFieldName.getFieldName(tableAndFieldName));
                    EtkMultiSprache multi = new EtkMultiSprache();
                    if (dbField != null) {
                        multi.setText(exportHeaderLang, dbField.getDisplayText(exportHeaderLang, dataBaseFallbackLanguages));
                    } else {
                        multi.setText(exportHeaderLang, TableAndFieldName.getFieldName(tableAndFieldName));
                    }
                    displayField.setText(multi);
                    displayField.setDefaultText(false);
                    headerFields.add(displayField);
                }
            } else {
                displayFieldList.remove(displayField);
                if (!DONT_SHOW_NAMES.contains(displayField.getKey().getName())) {
                    headerFields.add(displayField.cloneMe());
                }
            }
        }
        // zusätzliche Spalten hinzufügen
        for (String fieldName : EXTRA_COLUMNS_FIELD_NAMES) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DUMMY, fieldName, false, false);
            // Text noch setzen
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(exportHeaderLang, fieldName);
            if (fieldName.equals(FIELD_REMAINING_KGTU)) {
                multi.setText(exportHeaderLang, TranslationHandler.translateForLanguage("!!Zusätzliche KG/TU", exportHeaderLang));
            }
            displayField.setText(multi);
            displayField.setDefaultText(false);
            headerFields.add(displayField);
        }
        // restliche Spalten hinzufügen
        for (EtkDisplayField displayField : displayFieldList) {
            if (!DONT_SHOW_NAMES.contains(displayField.getKey().getName())) {
                headerFields.add(displayField.cloneMe());
            }
        }

        // Überschriften Zeile bilden
        for (EtkDisplayField displayField : headerFields) {
            header.add(displayField.getText().getTextByNearestLanguage(exportHeaderLang, dataBaseFallbackLanguages));
        }

        // Ausgabe Header
        exportWriter.writeHeader(header);

        transferredPartListEntries = new HashMap<>();
        aaDetectedProductMap = new HashMap<>();
        einPASListMap = new HashMap<>();
    }


    private void writePartListEntry(EtkDataPartListEntry partListEntry, Set<KgTuId> kgTuIdSet) {
        VisObject visObject = projectForCalculation.getVisObject();
        List<String> elements = new DwList<>();
        // speziell für Ausgabe CSV-Datei: auch leere Einträge erzeugen ein 'feld'
        for (EtkDisplayField displayField : headerFields) {
            String tableAndFieldName = displayField.getKey().getName();
            String value = "";
            if (!EXTRA_COLUMNS_FIELD_NAMES.contains(displayField.getKey().getFieldName())) {
                value = partListEntry.getFieldValueFromAllPartListTables(tableAndFieldName, exportLineLang);
                // direkte DB Ausgabe
                if (!DB_FORMAT_NAMES.contains(tableAndFieldName)) {
                    value = visObject.asText(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                             value, exportLineLang);
                }
            } else {
                String fieldName = TableAndFieldName.getFieldName(tableAndFieldName);
                // hier KG/TU-Werte einsetzen
                if (!kgTuIdSet.isEmpty()) {
                    Iterator<KgTuId> iterator = kgTuIdSet.iterator();
                    KgTuId firstElement = iterator.next();
                    switch (fieldName) {
                        case FIELD_KG:
                            value = firstElement.getKg();
                            break;
                        case FIELD_TU:
                            value = firstElement.getTu();
                            break;
                        case FIELD_REMAINING_KGTU:
                            while (iterator.hasNext()) {
                                KgTuId kgTuId = iterator.next();
                                if (!value.isEmpty()) {
                                    value = value + ", ";
                                }
                                value = value + kgTuId.toString("/");
                            }
                            break;
                    }
                }
            }
            elements.add(value);
        }
        exportWriter.writeToZipStream(elements);
    }

    private EtkDisplayField findDisplayField(String tableAndFieldName, List<EtkDisplayField> displayFieldList) {
        for (EtkDisplayField displayField : displayFieldList) {
            if (displayField.getKey().getName().equals(tableAndFieldName)) {
                return displayField;
            }
        }
        return null;
    }
}
