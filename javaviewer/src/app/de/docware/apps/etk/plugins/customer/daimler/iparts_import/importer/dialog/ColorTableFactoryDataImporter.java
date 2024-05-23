/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsFIKZValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGWithPEMDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * Importer für Werkseinsatzdaten zu Inhalt Farbtabelle (WX9/VX9), Zuordnung Teil-Farbtabelle (WX10/VX10) sowie
 * ereignisgesteuerte Werkseinsatzdaten zu Inhalt Farbtabelle (WY9)
 */
public class ColorTableFactoryDataImporter extends AbstractDIALOGWithPEMDataImporter implements iPartsConst {

    public static final String WY9_PREFIX = "WY9";
    public static final String TABLENAME_WX10 = TABLE_NAME_PREFIX + iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue();
    public static final String TABLENAME_WX9 = TABLE_NAME_PREFIX + iPartsFactoryDataTypes.COLORTABLE_CONTENT.getDatasetValue();
    public static final String TABLENAME_WY9 = TABLE_NAME_PREFIX + WY9_PREFIX;
    public static final String TABLENAME_VX10 = TABLE_NAME_PREFIX + iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue(); // Aftersales Verwendungsdaten zur Farbtabelle/Teile (T10RVX10);
    public static final String TABLENAME_VX9 = TABLE_NAME_PREFIX + iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue(); // Aftersales Verwendungsdaten zur Farbtabelle/Zuordnung Teile zu Farbe (T10RVX9);

    public static final String WK = "WK";                  // nur bei WXX Daten
    public static final String FT = "FT";
    public static final String POS = "POS";
    public static final String SDA = "SDA";
    public static final String ADAT = "ADAT";              // nur bei WXX Daten
    public static final String SDB = "SDB";
    public static final String FIKZ = "FIKZ";              // nur bei WXX Daten
    public static final String PEMA = "PEMA";
    public static final String PEMB = "PEMB";
    public static final String PEMTA = "PEMTA";
    public static final String PEMTB = "PEMTB";
    public static final String STCA = "STCA";              // nur bei WXX Daten
    public static final String STCB = "STCB";              // nur bei WXX Daten
    public static final String FRGKZ1 = "FRGKZ1";          // nur bei WXX Daten
    public static final String TEIL = "TEIL";              // nur bei WX10 Daten
    public static final String ETKZ = "ETKZ";              // nur bei VXX Daten
    public static final String STCODEAB = "STCODEAB";      // nur bei VXX Daten
    public static final String STCODEBIS = "STCODEBIS";    // nur bei VXX Daten
    public static final String WERK = "WERK";              // nur bei VXX Daten

    public static final String WX9_WX10_OLD_SUFFIX = "_ALT"; // Daten vor der Jahresbereinigung -> soll bei WX9 und WX10 verwendet werden

    public static final String WY9_EREIA = "WY9_EREIA";
    public static final String WY9_EREIB = "WY9_EREIB";

    // Zu überspringende Werkskennzeichnung
    private static final String INVALID_FACTORY = "000";

    private static final String FACTORY_FOR_ETKZ_ONLY = "0000";
    // Löschkennzeichen innerhalb eines Datensatzes (internes Löschkennzeichen)
    private static final String COLOR_FACTORY_DELETE_SIGN = "L";

    private final String importTableInXML;
    private final String dbTableName;
    private HashMap<String, String> mapping;
    private String[] primaryKeysImport;
    private String prefixForImporterInstance;
    private iPartsFactoryDataTypes currentDatasetId;

    private Map<IdWithType, Boolean> colorTableDataExistsCache;
    private Map<IdWithType, Boolean> colorTableContentIdExistsCache;
    private Map<String, iPartsDataColorTableToPart> referencedPartsCache;
    private Map<String, Set<iPartsDialogBCTEPrimaryKey>> seriesAndPartToBCTEKeysCache;
    private Map<String, RelevantUsagesOfColorTable> relevantUsagesOfColorTableCache;
    // Werksdaten gruppiert nach Werksdaten-ID ohne ADAT
    private Map<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>> factoryDataWithSameSDATAMap;
    // Werksdaten mit Löschkennzeichen gruppiert nach Werksdaten-ID ohne ADAT
    private Map<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>> factoryDataWithDeletedFlag;

    private Map<iPartsColorTableFactoryId, VXDataObjectForIdWithoutAdat> processedVXDatasets; // Für VX Import. Id ist die Id ohne ADAT

    private boolean importToDB = true;
    private HashSet<Long> adatAsLongSet;
    private ColorTableFactoryImportHelper factoryImportHelper;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ColorTableFactoryDataImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        this.dbTableName = TABLE_DA_COLORTABLE_FACTORY;
        initImporter();
    }

    private String getSourceFieldName(String fieldNameSuffix) {
        // Bei PEMA, PEMTA und STCA müssen bei WX9 und WX10 die Quellfeldnamen *_ALT verwendet werden (Daten vor der Jahresbereinigung)
        if ((importTableInXML.equals(TABLENAME_WX9) || importTableInXML.equals(TABLENAME_WX10) || importTableInXML.equals(TABLENAME_WY9))
            && (fieldNameSuffix.equals(PEMA) || fieldNameSuffix.equals(PEMTA) || fieldNameSuffix.equals(STCA))) {
            return prefixForImporterInstance + fieldNameSuffix + WX9_WX10_OLD_SUFFIX;
        } else {
            return prefixForImporterInstance + fieldNameSuffix;
        }
    }

    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";

        // Unterscheidung WX10 - WX9 - VX10 - VX9 - WY9
        if (importTableInXML.equals(TABLENAME_WX9)) {
            initSimilarData(iPartsFactoryDataTypes.COLORTABLE_CONTENT, getSourceFieldName(STCA),
                            prefixForImporterInstance + STCB);
            nameForImport = DCCF_COLORTABLE_CONTENT_FACTORY;
            importName = "!!DIALOG-Werkseinsatzdaten für Farbtabelleninhalt (WX9)";
        } else if (importTableInXML.equals(TABLENAME_WY9)) {
            initSimilarData(iPartsFactoryDataTypes.COLORTABLE_CONTENT, getSourceFieldName(STCA),
                            prefixForImporterInstance + STCB);
            // Muss hier extra gesetzt werden, da setCurrentDataBaseId() den Wert aus iPartsFactoryDataTypes.COLORTABLE_CONTENT nimmt
            prefixForImporterInstance = WY9_PREFIX + "_";
            nameForImport = DCCF_COLORTABLE_CONTENT_FACTORY_EVENT;
            importName = "!!DIALOG-Werkseinsatzdaten für Farbtabelleninhalt (WY9)";
            mapping.put(FIELD_DCCF_EVENT_FROM, WY9_EREIA);
            mapping.put(FIELD_DCCF_EVENT_TO, WY9_EREIB);
        } else if (importTableInXML.equals(TABLENAME_WX10)) {
            initSimilarData(iPartsFactoryDataTypes.COLORTABLE_PART, getSourceFieldName(STCA),
                            prefixForImporterInstance + STCB);
            nameForImport = DCCF_COLORTABLE_PART_FACTORY;
            importName = "!!DIALOG-Werkseinsatzdaten für Teil zu Farbtabelle (WX10)";
        } else if (importTableInXML.equals(TABLENAME_VX9)) {
            // After-Sales Verwendungsdaten für Farbtabelleninhalt (VX9)
            initSimilarData(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS, prefixForImporterInstance + STCODEAB,
                            prefixForImporterInstance + STCODEBIS);
            nameForImport = DCCF_AS_COLORTABLE_CONTENT_FACTORY;
            importName = "!!DIALOG-After-Sales Verwendungsdaten für Farbtabelleninhalt (VX9)";
        } else if (importTableInXML.equals(TABLENAME_VX10)) {
            // After-Sales Verwendungsdaten für Teil zu Farbtabelle (VX10)
            initSimilarData(iPartsFactoryDataTypes.COLORTABLE_PART_AS, prefixForImporterInstance + STCODEAB,
                            prefixForImporterInstance + STCODEBIS);
            nameForImport = DCCF_AS_COLORTABLE_PART_FACTORY;
            importName = "!!DIALOG-After-Sales Verwendungsdaten für Teil zu Farbtabelle (VX10)";
        }

        // Teil des Mappings, das für alle Importe gleich ist.
        mapping.put(FIELD_DCCF_SDATB, prefixForImporterInstance + SDB);
        mapping.put(FIELD_DCCF_PEMA, getSourceFieldName(PEMA));
        mapping.put(FIELD_DCCF_PEMB, prefixForImporterInstance + PEMB);
        mapping.put(FIELD_DCCF_PEMTA, getSourceFieldName(PEMTA));
        mapping.put(FIELD_DCCF_PEMTB, prefixForImporterInstance + PEMTB);

        boolean removeExistingDataSelectable = true;
        // Die Primärschlüsselfelder festlegen
        if (importTableInXML.equals(TABLENAME_WX10) || isFactoryContentData()) {
            primaryKeysImport = new String[]{ prefixForImporterInstance + FT, prefixForImporterInstance + SDA, prefixForImporterInstance + POS,
                                              prefixForImporterInstance + WK, prefixForImporterInstance + ADAT };

        } else if ((importTableInXML.equals(TABLENAME_VX9)) || (importTableInXML.equals(TABLENAME_VX10))) {

            primaryKeysImport = new String[]{ prefixForImporterInstance + FT, prefixForImporterInstance + SDA,
                                              prefixForImporterInstance + POS, prefixForImporterInstance + WERK };
            removeExistingDataSelectable = false;
        }

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(dbTableName,
                                                                                         nameForImport, false, false, removeExistingDataSelectable,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
        // PEM Stammdaten sollen nur bei Produktionsdaten gesammelt werden (nicht AS)
        if (isProductionData()) {
            clearPEMs();
        } else {
            disablePEMImport();
        }
    }

    private boolean isFactoryContentData() {
        return importTableInXML.equals(TABLENAME_WX9) || importTableInXML.equals(TABLENAME_WY9);
    }

    private boolean isProductionData() {
        return isFactoryContentData() || importTableInXML.equals(TABLENAME_WX10);
    }

    /**
     * Initialisiert gleiche Strukturen mit unterschiedlichen Werten abhängig vom Importtyp
     *
     * @param factoryDataTypes
     * @param stcaElement
     * @param stcbElement
     */
    private void initSimilarData(iPartsFactoryDataTypes factoryDataTypes, String stcaElement, String stcbElement) {
        setCurrentDataBaseId(factoryDataTypes);
        mapping.put(FIELD_DCCF_STCA, stcaElement);
        mapping.put(FIELD_DCCF_STCB, stcbElement);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(primaryKeysImport);

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(TABLE_NAME_PREFIX + currentDatasetId.getDatasetValue())
                   || importer.getTableNames().get(0).equals(TABLENAME_WY9)
                   || importer.getTableNames().get(0).equals(WY9_PREFIX)
                   || importer.getTableNames().get(0).equals(iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())
                   || importer.getTableNames().get(0).equals(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())
                   || importer.getTableNames().get(0).equals(iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue());
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();

        // WICHTIG: für die Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
        colorTableDataExistsCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        colorTableContentIdExistsCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        referencedPartsCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        seriesAndPartToBCTEKeysCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        relevantUsagesOfColorTableCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        processedVXDatasets = new HashMap<>();
        adatAsLongSet = new HashSet<>();
        factoryDataWithSameSDATAMap = new HashMap<>();
        factoryDataWithDeletedFlag = new HashMap<>();
        factoryImportHelper = new ColorTableFactoryImportHelper(getProject(), mapping, dbTableName);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        if (importTableInXML.equals(TABLENAME_WX10) || isFactoryContentData()) {
            importWXxRecord(importRec, recordNo);
        } else if ((importTableInXML.equals(TABLENAME_VX9)) || (importTableInXML.equals(TABLENAME_VX10))) {
            importVXxRecord(importRec, recordNo);
        }
    }

    /**
     * Importroutine für einen Datensatz der DIALOG-Tabellen [VX10] und [VX9] in die DB Tabelle [DA_COLORTABLE_FACTORY]
     *
     * @param importRec
     * @param recordNo
     */
    private void importVXxRecord(Map<String, String> importRec, int recordNo) {

        // Falscher Importtabellenname ist das Abbruchkriterium:
        if (!(importTableInXML.equals(TABLENAME_VX10)) && !(importTableInXML.equals(TABLENAME_VX9))) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (Import Tabelle nicht bekannt: %2)",
                                         String.valueOf(recordNo), importTableInXML));
            reduceRecordCount();
            return;
        }

        // Über den Helper spezielle Felder extrahieren/interpretieren
        String factoryNumber = factoryImportHelper.getFactoryVXx(importRec);
        String colorTableId = factoryImportHelper.getColorTableId(importRec);

        // Die Fahrzeugbaureihe aus der Variantentabellen-ID bilden und auf Importrelevanz prüfen.
        if (!factoryImportHelper.checkImportRelevanceForSeriesFromColortable(colorTableId, getInvalidSeriesSet(), this)) {
            return;
        }

        String sdata = factoryImportHelper.getSData(importRec);
        String pos = factoryImportHelper.getPos(importRec);

        checkIfColorTableDataExists(recordNo, colorTableId);

        // Mehrfach verwendete Variablen initialisieren:
        IdWithType id;
        EtkDataObject referencedDataObject;
        boolean referenceExists = false;
        String referencedSeriesNumber = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        String etkzDBField;
        String importRecEtkz = importRec.get(prefixForImporterInstance + ETKZ);
        String referenceTable;
        Set<String> referencedPartNumbers = new HashSet<>();
        boolean isInitialDataImport = isDIALOGInitialDataImport();
        iPartsDataReleaseState datasetState = isInitialDataImport ? iPartsDataReleaseState.RELEASED : iPartsDataReleaseState.IMPORT_DEFAULT;
        iPartsDataReleaseState referencedDataObjectState = null;

        if (factoryNumber.equals(INVALID_FACTORY)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        prefixForImporterInstance + WERK, factoryNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        } else {
            // Überprüfen, ob zum aktuellen Datensatz ein Referenzeintrag existiert (bei VX10 -> X10E=[DA_COLORTABLE_PART])
            if (importTableInXML.equals(TABLENAME_VX10)) {
                referenceTable = TABLE_DA_COLORTABLE_PART;
                etkzDBField = FIELD_DCTP_ETKZ;
                // ID und DataObject bauen
                id = new iPartsColorTableToPartId(colorTableId, pos, sdata);
                String cacheKey = id.toString("|");
                referencedDataObject = referencedPartsCache.get(cacheKey);
                // Datensatz mit Original-POS liegt noch nicht im Cache
                if (referencedDataObject == null) {
                    // Suche nach einem Datensatz, der eine Teilenummer-POS hat und zusätzlich die Original-POS
                    iPartsDataColorTableToPartList existingParts = iPartsDataColorTableToPartList.loadColorTableToPartListWithOriginalPos(getProject(),
                                                                                                                                          colorTableId,
                                                                                                                                          sdata, pos);
                    // Liste ist leer -> Es gibt keinen Datensatz mit übereinstimmender Original-POS
                    if (!existingParts.isEmpty()) {
                        referencedDataObject = existingParts.get(0);
                        referencedPartsCache.put(cacheKey, (iPartsDataColorTableToPart)referencedDataObject);
                    }
                }

                // Wenn ein Referenzeintrag existiert, dann kann der aktuelle VX10 Datensatz mit einer Teilenummer-POS
                // gespeichert werden. So kann später ein Zusammenhang zum X10E Datensatz hergestellt werden. Die Original-POS
                // wird weiter unten importiert.
                if (referencedDataObject != null) {
                    referenceExists = true;
                    String partNumber = ((iPartsDataColorTableToPart)referencedDataObject).getPartNumber();
                    pos = makePseudoPos(partNumber, pos);
                    referencedPartNumbers.add(partNumber);
                }
                if (!isInitialDataImport) {
                    if (!referenceExists || !iPartsFactories.getInstance(getProject()).isValidForFilter(factoryNumber)) {
                        // Status wird direkt auf "freigegeben" gesetzt, wenn:
                        // 1. Datensätze aus der Urladung kommen
                        // 2. es sich um Datensätze ohne referenzierten  X10E-Satz (DA_COLORTABLE_PART) handelt
                        // 3. das Werk irrelevant ist
                        datasetState = iPartsDataReleaseState.RELEASED;
                    } else {
                        Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = getRelevantBCTEPrimaryKeys(referencedSeriesNumber, (iPartsDataColorTableToPart)referencedDataObject, factoryImportHelper);
                        datasetState = factoryImportHelper.calculateReleaseStateByUsage(relevantBCTEPrimaryKeys);
                    }
                }
            } else { // ... (bei VX9 -> X9E=[DA_COLORTABLE_CONTENT])
                referenceTable = TABLE_DA_COLORTABLE_CONTENT;
                etkzDBField = FIELD_DCTC_ETKZ;
                // ID und DataObject bauen
                id = new iPartsColorTableContentId(colorTableId, pos, sdata);
                referencedDataObject = new iPartsDataColorTableContent(getProject(), (iPartsColorTableContentId)id); // X9E
                // In der übergeordneten Tabelle [DA_COLORTABLE_CONTENT(aus X9E)] kontrollieren, ob ein Datensatz existiert.
                referenceExists = colorTableContentEntryExists((iPartsColorTableContentId)id, (iPartsDataColorTableContent)referencedDataObject);
                if (!isInitialDataImport) {
                    if (!referenceExists || !iPartsFactories.getInstance(getProject()).isValidForFilter(factoryNumber)) {
                        // Status wird direkt auf "freigegeben" gesetzt, wenn:
                        // 1. Datensätze aus der Urladung kommen
                        // 2. es sich um Datensätze ohne referenzierten X9E-Satz (DA_COLORTABLE_CONTENT) handelt
                        // 3. das Werk irrelevant ist
                        datasetState = iPartsDataReleaseState.RELEASED;
                    } else {
                        RelevantUsagesOfColorTable relevantBCTEPrimaryKeysAndPartNumbers = getRelevantBCTEPrimaryKeysAndPartNumbers((iPartsDataColorTableContent)referencedDataObject);
                        Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = relevantBCTEPrimaryKeysAndPartNumbers.getRelevantBCTEPrimaryKeys();
                        referencedPartNumbers = relevantBCTEPrimaryKeysAndPartNumbers.getRelevantPartNumbers();
                        datasetState = factoryImportHelper.calculateReleaseStateByUsage(relevantBCTEPrimaryKeys);
                    }
                }
                // DAIMLER-10055: Falls in VX9 das ETKZ des Referenzobjekts neu gesetzt wird (weiter unten im Code), muss
                // auch der Status angepasst werden
                // Wenn der bisherige Ersatzteilkenner <> " " war und jetzt " " wird, soll der Status "freigegeben" gesetzt werden
                // Wenn der bisherige Ersatzteilkenner = " " war und jetzt <> " " wird, soll der Status "nicht relevant" gesetzt werden
                // Wenn sich der Ersatzteilkenner nicht ändert bzw. schon einen Wert hat und einen anderen bekommt
                // (aktuell nur N und K möglich), bleibt der Status unverändert
                if (referenceExists) {
                    String referencedDataObjectETKZ = referencedDataObject.getFieldValue(etkzDBField);
                    // Ist keine von beiden leer, dann bleibt der Status, wie er gerade ist
                    if (!StrUtils.isValid(referencedDataObjectETKZ, importRecEtkz)) {
                        if (StrUtils.isEmpty(referencedDataObjectETKZ) && StrUtils.isValid(importRecEtkz)) {
                            referencedDataObjectState = iPartsDataReleaseState.NOT_RELEVANT;
                        } else if (StrUtils.isValid(referencedDataObjectETKZ) && StrUtils.isEmpty(importRecEtkz)) {
                            referencedDataObjectState = iPartsDataReleaseState.RELEASED;
                        }
                    }
                }
            }

            boolean isMarkedForDeletion = DIALOGImportHelper.isDatasetMarkedForDeletion(importRec);
            if (isMarkedForDeletion) {
                // Im Fall von Löschdatensätzen bedeutet NEW dann CHECK_DELETION und RELEASED dann DELETED
                if (datasetState == iPartsDataReleaseState.NEW) {
                    datasetState = iPartsDataReleaseState.CHECK_DELETION;
                } else {
                    datasetState = iPartsDataReleaseState.DELETED;
                }
            }

            iPartsColorTableFactoryId colorTableFactoryIdWithoutAdat = new iPartsColorTableFactoryId(colorTableId, pos, factoryNumber,
                                                                                                     "", currentDatasetId.getDbValue(), sdata);
            VXDataObjectForIdWithoutAdat vxDataObjectWithAllDatasets = processedVXDatasets.get(colorTableFactoryIdWithoutAdat);
            if (vxDataObjectWithAllDatasets == null) {
                vxDataObjectWithAllDatasets = new VXDataObjectForIdWithoutAdat();
                vxDataObjectWithAllDatasets.setReferencedSeriesNumber(referencedSeriesNumber);
                vxDataObjectWithAllDatasets.setReferencedPartNumbers(referencedPartNumbers);
                iPartsDataColorTableFactoryList colorTableFactoryList = iPartsDataColorTableFactoryList.loadAllColorTableFactoryForColorTableFactoryId(getProject(), colorTableFactoryIdWithoutAdat);
                vxDataObjectWithAllDatasets.setDBData(colorTableFactoryList.getAsList());
                processedVXDatasets.put(colorTableFactoryIdWithoutAdat, vxDataObjectWithAllDatasets);
            }

            // Referenzeintrag existiert nicht -> Warnung ausgeben, aber trotzdem importieren
            if (!referenceExists) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig (Keinen passenden Referenzeintrag gefunden. Referenztabelle: %2; Referenzschlüssel: %3)",
                                                            String.valueOf(recordNo), referenceTable, id.toString("|")),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);

                // Wenn der Referenzdatensatz nicht gefunden wurde, dann haben wir auch keine Teilenummer, die wir als POS
                // nutzen könnten. Daher muss man hier abbrechen.
                if (importTableInXML.equals(TABLENAME_VX10)) {
                    getMessageLog().fireMessage(translateForLog("!!VX10 Datensatz in Record %1 übersprungen, da keine passende Teilenummer " +
                                                                "über den Referenzeintrag gefunden werden konnte. Farbtabelle \"%2\"," +
                                                                " Datum ab \"%3\", Original-POS \"%4\"",
                                                                String.valueOf(recordNo), colorTableId, sdata, pos),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return;
                }
            } else {
                // Wenn der übergeordnete Datensatz existiert, dort das Ersatzteilkennzeichen setzen ...
                // ... aber nur, wenn das Werk FACTORY_FOR_ETKZ_ONLY ("0000") ist.
                if (factoryNumber.equals(FACTORY_FOR_ETKZ_ONLY)) {
                    // Ein neuer Status wid nur dann gesetzt, wenn es sich um den VX9 Importer handelt
                    // und die Bedingungen wie aufgeführt erfüllt sind
                    // Dann ist referencedDataObjectState gefüllt
                    if (referencedDataObjectState != null) {
                        referencedDataObject.setFieldValue(iPartsConst.FIELD_DCTC_STATUS, referencedDataObjectState.getDbValue(), DBActionOrigin.FROM_EDIT);
                    }
                    referencedDataObject.setFieldValue(etkzDBField, importRecEtkz, DBActionOrigin.FROM_EDIT);
                    if (importToDB) {
                        saveToDB(referencedDataObject);
                    }
                }
            }

            if (!factoryNumber.equals(FACTORY_FOR_ETKZ_ONLY)) { // kein Import für FACTORY_FOR_ETKZ_ONLY ("0000")
                // Bei VX-Import muss der Zeitstempel (DCCF_ADAT) künstlich gesetzt werden, da kein entsprechender Wert in der Importdatei vorhanden ist.
                // Und: Jede Änderung muss den Zeitstempel erhöhen um so etwas wie eine Historie zu bekommen.
                iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(colorTableId, pos, factoryNumber,
                                                                                              iPartsDialogDateTimeHandler.getNextDBDateTimeForExistingDateTimes(adatAsLongSet),
                                                                                              currentDatasetId.getDbValue(), sdata);
                iPartsDataColorTableFactory dataColorTableFactory = new iPartsDataColorTableFactory(getProject(), colorTableFactoryId);

                if (!dataColorTableFactory.existsInDB()) {
                    dataColorTableFactory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    dataColorTableFactory.setFieldValue(FIELD_DCCF_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
                    factoryImportHelper.fillOverrideCompleteDataForDIALOGReverse(dataColorTableFactory, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Der Datensatz in Record %1 wird übersprungen, da bereits ein Eintrag in der Datenbank mit gleichem Primärschlüssel existiert" +
                                                                " (%2). Das kann eigentlich nicht passieren, da neue Einträge mit neuem ADAT angelegt werden müssten",
                                                                String.valueOf(recordNo), colorTableFactoryId.toString()),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return;
                }

                // Status setzen falls leer
                if (isInitialDataImport || dataColorTableFactory.getFieldValue(iPartsConst.FIELD_DCCF_STATUS).isEmpty()) {
                    dataColorTableFactory.setFieldValue(iPartsConst.FIELD_DCCF_STATUS, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
                }

                // Füge die Original-POS hinzu (nur bei VX10 und WX10)
                addSourcePos(dataColorTableFactory, factoryImportHelper, importRec);

                // VX Datensätze können im Gegensatz zu WX Datensätzen Löschkennzeichen enthalten
                if (isMarkedForDeletion) {
                    // VX Datensätze werden im postImportTask gespeichert
                    vxDataObjectWithAllDatasets.addImportedDeletionData(dataColorTableFactory, this, mapping);
                } else {
                    vxDataObjectWithAllDatasets.addImportedData(dataColorTableFactory, this, mapping);
                }
            }
        }
    }

    /**
     * Erstellt aus der übergebenen Teilenummer + Prefix die Pseudo-Position bzw. liefert bei fehlender Teilenummer die
     * <i>originalPos</i> zurück.
     *
     * @param partNumber
     * @param originalPos
     * @return
     */
    public static String makePseudoPos(String partNumber, String originalPos) {
        if (StrUtils.isValid(partNumber)) {
            return POS_PREFIX + partNumber;
        }
        return originalPos;
    }

    /**
     * Importroutine für einen Datensatz der DIALOG-Tabellen [WX9] und [WX10] in die DB Tabelle [DA_COLORTABLE_FACTORY]
     *
     * @param importRec
     * @param recordNo
     */

    private void importWXxRecord(Map<String, String> importRec, int recordNo) {
        // PEM Stamm anlegen (nur für PEM ab)
        handlePEMData(importRec, factoryImportHelper);
        String colorTableId = factoryImportHelper.getColorTableId(importRec);
        // Die Fahrzeugbaureihe aus der Variantentabellen-ID bilden.
        if (!factoryImportHelper.checkImportRelevanceForSeriesFromColortable(colorTableId, getInvalidSeriesSet(), this)) {
            return;
        }

        // Nur folgende FIKZ: "B", "F" und "S" sollen berücksichtigt werden. Alle anderen werden übersprungen.
        String fikz = factoryImportHelper.getFikz(importRec);
        iPartsFIKZValues fikzType = iPartsFIKZValues.getTypeFromCode(fikz);
        if ((fikzType != iPartsFIKZValues.MANUAL) && (fikzType != iPartsFIKZValues.COLOR_ISSUES) && (fikzType != iPartsFIKZValues.HOLE_PATTERN)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen (FIKZ für Import nicht relevant. FIKZ Bedeutung: %4)",
                                                        String.valueOf(recordNo), "FIKZ", fikz, fikzType.getDescriptionFromDIALOG()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        checkIfColorTableDataExists(recordNo, colorTableId);

        IdWithType id;
        String factoryNumber = factoryImportHelper.getFactoryXxP(importRec);
        String refTableName;
        boolean referenceExists = false;
        String referencedSeriesNumber = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        Set<String> referencedPartNumbers = new HashSet<>();
        Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys;
        iPartsDataReleaseState datasetState;
        // Datum ab muss in die richtige Form gebracht werden
        String sdata = factoryImportHelper.getSData(importRec);
        String pos = factoryImportHelper.getPos(importRec);
        boolean isInitialDataImport = isDIALOGInitialDataImport();
        // Überprüfen, ob zum aktuellen Datensatz ein Referenzeintrag existiert (bei WX10 -> X10E, bei WX9 -> X9E)
        if (importTableInXML.equals(TABLENAME_WX10)) {
            refTableName = TABLE_DA_COLORTABLE_PART;
            // ID bauen
            pos = makePseudoPos(factoryImportHelper.handleValueOfSpecialField(prefixForImporterInstance + TEIL, importRec), pos);
            id = new iPartsColorTableToPartId(colorTableId, pos, sdata);
            iPartsDataColorTableToPart colorTableToPart = getReferencedColorTableToPart(colorTableId, pos, sdata);
            if (colorTableToPart != null) {
                referenceExists = true;
                referencedPartNumbers.add(colorTableToPart.getPartNumber());
                // DAIMLER-5194: Die Werksdaten sollen mit den ermittelten X10E_SDA in iParts angelegt werden
                sdata = colorTableToPart.getFieldValue(FIELD_DCTP_SDATA);
            }
            if (isInitialDataImport || !referenceExists || !iPartsFactories.getInstance(getProject()).isValidForFilter(factoryNumber)) {
                // Status wird direkt auf "freigegeben" gesetzt, wenn:
                // 1. Datensätze aus der Urladung kommen
                // 2. es sich um Datensätze ohne referenzierten X10E-Satz (DA_COLORTABLE_PART) handelt
                // 3. das Werk irrelevant ist
                datasetState = iPartsDataReleaseState.RELEASED;
            } else {
                relevantBCTEPrimaryKeys = getRelevantBCTEPrimaryKeys(referencedSeriesNumber, colorTableToPart, factoryImportHelper);
                datasetState = factoryImportHelper.calculateReleaseStateByUsage(relevantBCTEPrimaryKeys);
            }
        } else if (isFactoryContentData()) {
            refTableName = TABLE_DA_COLORTABLE_CONTENT;
            // ID bauen
            id = new iPartsColorTableContentId(colorTableId, pos, sdata);
            iPartsDataColorTableContent colorTableContent = getReferencedColorTableContent(colorTableId, pos, sdata);
            if (colorTableContent != null) {
                referenceExists = true;
                // DAIMLER-5194: Die Werksdaten sollen mit den ermittelten X9E_SDA in iParts angelegt werden
                sdata = colorTableContent.getFieldValue(FIELD_DCTC_SDATA);
            }
            if (isInitialDataImport || !referenceExists || !iPartsFactories.getInstance(getProject()).isValidForFilter(factoryNumber)) {
                // Status wird direkt auf "freigegeben" gesetzt, wenn:
                // 1. Datensätze aus der Urladung kommen
                // 2. es sich um Datensätze ohne referenzierten X9E-Satz (DA_COLORTABLE_CONTENT) handelt
                // 3. das Werk irrelevant ist
                datasetState = iPartsDataReleaseState.RELEASED;
            } else {
                RelevantUsagesOfColorTable relevantBCTEPrimaryKeysAndPartNumbers = getRelevantBCTEPrimaryKeysAndPartNumbers(colorTableContent);
                datasetState = factoryImportHelper.calculateReleaseStateByUsage(relevantBCTEPrimaryKeysAndPartNumbers.getRelevantBCTEPrimaryKeys());
                referencedPartNumbers = relevantBCTEPrimaryKeysAndPartNumbers.getRelevantPartNumbers();
            }
        } else {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (Import Tabelle nicht bekannt: %2)",
                                         String.valueOf(recordNo), importTableInXML));
            reduceRecordCount();
            return;
        }
        if (!referenceExists) {
            // Referenzeintrag existiert nicht -> Warnung und trotzdem importieren
            getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig (Keinen passenden Referenzeintrag gefunden. Referenztabelle: %2; Referenzschlüssel: %3)",
                                                        String.valueOf(recordNo), refTableName, id.toString("|")),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
        // ADAT muss ins richtige Format konvertiert werden
        String adat = factoryImportHelper.getADat(importRec);
        // Check, ob Datensatz schon existiert. Falls nicht anlegen. Ansonsten updaten
        iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(colorTableId, pos, factoryNumber,
                                                                                      adat,
                                                                                      currentDatasetId.getDbValue(), sdata);
        // Werksdaten ID ohne ADAT, weil die zeitliche Reihenfolge der Datensätze vom Original-SDA abhängt
        iPartsColorTableFactoryId factoryDataIdWithoutAdat = new iPartsColorTableFactoryId(colorTableId, pos, factoryNumber, "",
                                                                                           currentDatasetId.getDbValue(), sdata);
        iPartsDataColorTableFactory dataColorTableFactory = new iPartsDataColorTableFactory(getProject(), colorTableFactoryId);
        if (!dataColorTableFactory.existsInDB()) {
            dataColorTableFactory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataColorTableFactory.setFieldValue(FIELD_DCCF_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
        }
        // ... Im Fall von MAD wird der existierende Datensatz überschrieben
        factoryImportHelper.deleteContentIfMADSource(dataColorTableFactory, FIELD_DCCF_SOURCE, true);
        factoryImportHelper.fillOverrideCompleteDataForDIALOGReverse(dataColorTableFactory, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);

        // Original-SDA setzen
        dataColorTableFactory.setFieldValue(FIELD_DCCF_ORIGINAL_SDATA, factoryImportHelper.getSData(importRec), DBActionOrigin.FROM_EDIT);

        // Status setzen falls leer oder Urladung
        if (isInitialDataImport || dataColorTableFactory.getFieldValue(FIELD_DCCF_STATUS).isEmpty()) {
            dataColorTableFactory.setFieldValue(FIELD_DCCF_STATUS, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
        }

        addSourcePos(dataColorTableFactory, factoryImportHelper, importRec);

        // Löschkennzeichen setzen
        String deleteSign = factoryImportHelper.handleValueOfSpecialField(prefixForImporterInstance + FRGKZ1, importRec);
        boolean hasInternalDeleteSign = deleteSign.equals(COLOR_FACTORY_DELETE_SIGN);
        // Datensätze mit internen Löschkennzeichen werden in einer eigenen Map gehalten, weil sie entweder eigenständige
        // Datensätze oder Ergänzungen zu anderen Datensätze sein können
        if (hasInternalDeleteSign) {
            dataColorTableFactory.setFieldValueAsBoolean(FIELD_DCCF_IS_DELETED, true, DBActionOrigin.FROM_EDIT);

            Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> factoryDataWithSameSDATA = createOrGetDataMap(factoryDataWithDeletedFlag, factoryDataIdWithoutAdat);
            factoryDataWithSameSDATA.put(dataColorTableFactory.getAsId(), new WXDataObjectWithReferencedParts(dataColorTableFactory, referencedPartNumbers));
        } else {
            Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> factoryDataWithSameSDATA = createOrGetDataMap(factoryDataWithSameSDATAMap, factoryDataIdWithoutAdat);
            WXDataObjectWithReferencedParts existingData = factoryDataWithSameSDATA.get(dataColorTableFactory.getAsId());
            if (existingData != null) {
                // Existiert zu einen eindeutigen Schlüssel (hier wichtig: SDA und ADAT) schon ein Datensatz, dann gewinnt
                // Der Datensatz mit dem höheren Original-SDA.
                if (dataColorTableFactory.getOriginalSdata().compareTo(existingData.getColorTableFactory().getOriginalSdata()) >= 0) {
                    factoryDataWithSameSDATA.put(dataColorTableFactory.getAsId(), new WXDataObjectWithReferencedParts(dataColorTableFactory, referencedPartNumbers));
                }
            } else {
                factoryDataWithSameSDATA.put(dataColorTableFactory.getAsId(), new WXDataObjectWithReferencedParts(dataColorTableFactory, referencedPartNumbers));
            }
        }
    }

    private Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> createOrGetDataMap(Map<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>> dataMap,
                                                                                               iPartsColorTableFactoryId factoryDataIdWithoutAdat) {
        return dataMap.computeIfAbsent(factoryDataIdWithoutAdat, k -> new HashMap<>());
    }

    private void checkIfColorTableDataExists(int recordNo, String colorTableId) {
        // Check, ob Farbtabelle existiert
        iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(colorTableId);
        iPartsDataColorTableData colorTableData = new iPartsDataColorTableData(getProject(), colorTableDataId);
        boolean referenceExists = colorTableEntryExists(colorTableDataId, colorTableData);

        if (!referenceExists) {
            // Wenn keine dazugehörige Farbtabelle existiert, kann auch keine Überprüfung der relevanten Werke erfolgen -> wir importieren trotzdem (mit Warnung)
            getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig. Farbtabelle \"%2\" existiert nicht in der Datenbank.",
                                                        String.valueOf(recordNo), colorTableDataId.getColorTableId()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

    /**
     * Liefert alle relevanten {@link iPartsDialogBCTEPrimaryKey}s für ein Teil zurück, aber nur für eine bestimmte Baureihe
     */
    private Set<iPartsDialogBCTEPrimaryKey> getRelevantBCTEPrimaryKeys(String seriesNumber, iPartsDataColorTableToPart colorTableToPart,
                                                                       ColorTableFactoryImportHelper importHelper) {
        String seriesAndPart = importHelper.getIdForBCTEPrimaryKeys(seriesNumber, colorTableToPart.getPartNumber());
        Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = seriesAndPartToBCTEKeysCache.get(seriesAndPart);
        if (relevantBCTEPrimaryKeys == null) {
            relevantBCTEPrimaryKeys = new HashSet<>();
            EtkDataObjectList<iPartsDataDialogData> dialogDataList =
                    iPartsDataDialogDataList.loadBCTEKeysForSeriesAndMatNr(getProject(), seriesNumber, colorTableToPart.getPartNumber());
            for (iPartsDataDialogData dialogData : dialogDataList) {
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogData.getAsId().getDialogGuid());
                relevantBCTEPrimaryKeys.add(bctePrimaryKey);
            }
            seriesAndPartToBCTEKeysCache.put(seriesAndPart, relevantBCTEPrimaryKeys);
        }
        return relevantBCTEPrimaryKeys;
    }

    /**
     * Liefert alle relevanten {@link iPartsDialogBCTEPrimaryKey}s und Teile, auf die sich die Farbtabelle bezieht,
     * auf die sich wiederum der übergegene Farbtabelleninhalt bezieht. Aber nur die Teile mit BCTEKeys mit der Baureihe der Farbtabelle
     */
    private RelevantUsagesOfColorTable getRelevantBCTEPrimaryKeysAndPartNumbers(iPartsDataColorTableContent colorTableContent) {
        String colorTableId = colorTableContent.getAsId().getColorTableId();
        RelevantUsagesOfColorTable relevantUsages = relevantUsagesOfColorTableCache.get(colorTableId);
        if (relevantUsages == null) {
            Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys = new HashSet<>();
            Set<String> referencedPartNumbers = new HashSet<>();
            iPartsDataColorTableToPartList colorTableToPartList =
                    iPartsDataColorTableToPartList.loadWithGUIDandPartNoForColorTable(getProject(), colorTableContent.getAsId().getColorTableId());
            for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartList) {
                String bctePrimaryKeyString = colorTableToPart.getFieldValue(FIELD_DD_GUID);
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bctePrimaryKeyString);
                relevantBCTEPrimaryKeys.add(bctePrimaryKey);
                referencedPartNumbers.add(colorTableToPart.getPartNumber());
            }
            relevantUsages = new RelevantUsagesOfColorTable(referencedPartNumbers, relevantBCTEPrimaryKeys);
            relevantUsagesOfColorTableCache.put(colorTableId, relevantUsages);
        }
        return relevantUsages;
    }

    /**
     * Liefert das referenzierte {@link iPartsDataColorTableToPart} zurück, dessen SDATA-SDATB-Intervall sdata umschließt
     */
    private iPartsDataColorTableToPart getReferencedColorTableToPart(String colorTableId, String pos, String importSDATA) {
        EtkDataObjectList<iPartsDataColorTableToPart> colorTableToPartList = iPartsDataColorTableToPartList.loadColorTableToPartListForPartNumberAndColortableId(getProject(), colorTableId, pos);
        iPartsDataColorTableToPart result = null;
        for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartList) {
            String entrySdata = colorTableToPart.getFieldValue(FIELD_DCTP_SDATA);
            if (isInIntervalOfEntry(entrySdata, importSDATA, colorTableToPart.getFieldValue(FIELD_DCTP_SDATB))) {
                // DAIMLER-5194: nimm das colorTableToPart, dessen DCTP_SDATA <= SDA und DCTP_SDATB > SDA des importRecords ist
                if ((result == null) || (entrySdata.compareTo(result.getAsId().getSDATA()) > 0)) {
                    result = colorTableToPart;
                }
            }
        }
        return result;
    }

    /**
     * returned das referenzierte {@link iPartsDataColorTableContent}, dessen SDATA-SDATB-Intervall sdata umschließt
     */
    private iPartsDataColorTableContent getReferencedColorTableContent(String colorTableId, String pos, String importSDATA) {
        EtkDataObjectList<iPartsDataColorTableContent> colorTableContentList = iPartsDataColorTableContentList.loadColorTableContentListForPartNumberAndColortableId(getProject(), colorTableId, pos);
        iPartsDataColorTableContent result = null;
        for (iPartsDataColorTableContent colorTableContent : colorTableContentList) {
            String entrySdata = colorTableContent.getFieldValue(FIELD_DCTC_SDATA);
            if (isInIntervalOfEntry(entrySdata, importSDATA, colorTableContent.getFieldValue(FIELD_DCTC_SDATB))) {
                if ((result == null) || (entrySdata.compareTo(result.getAsId().getSDATA()) > 0)) {
                    result = colorTableContent;
                }
            }
        }
        return result;
    }

    /**
     * @return true, wenn entrySDATA <= importSDATA < entrySDATB gilt
     */
    public boolean isInIntervalOfEntry(String entrySDATA, String importSDATA, String entrySDATB) {
        if (entrySDATA.equals("")) {
            entrySDATA = iPartsDialogDateTimeHandler.MINIMUM_STATE_DATETIME;
        }
        if (entrySDATB.equals("")) {
            entrySDATB = iPartsDialogDateTimeHandler.FINAL_STATE_DATETIME;
        }
        if ((entrySDATA.compareTo(importSDATA) <= 0) && (importSDATA.compareTo(entrySDATB) < 0)) {
            return true;
        }
        return false;
    }

    /**
     * Fügt dem Werkeinsatzdaten-Objekt den ursprünglichen POS Wert hinzu (nur bei WX10 und VX10 Datensätzen)
     *
     * @param dataColorTableFactory
     * @param factoryImportHelper
     * @param importRec
     */
    private void addSourcePos(iPartsDataColorTableFactory dataColorTableFactory, ColorTableFactoryImportHelper factoryImportHelper,
                              Map<String, String> importRec) {
        if (importTableInXML.equals(TABLENAME_VX10) || importTableInXML.equals(TABLENAME_WX10)) {
            String originalPos = factoryImportHelper.handleValueOfSpecialField(prefixForImporterInstance + POS, importRec);
            dataColorTableFactory.setFieldValue(FIELD_DCCF_POS_SOURCE, originalPos, DBActionOrigin.FROM_EDIT);
        }
    }

    private boolean colorTableContentEntryExists(iPartsColorTableContentId colorTableContentId, iPartsDataColorTableContent colorTableContentObject) {
        return existsInDBWithCache(colorTableContentIdExistsCache, colorTableContentId, colorTableContentObject);
    }

    private boolean colorTableEntryExists(iPartsColorTableDataId idcolorTableDataId, iPartsDataColorTableData dataColorTableData) {
        return existsInDBWithCache(colorTableDataExistsCache, idcolorTableDataId, dataColorTableData);
    }

    @Override
    protected void postImportTask() {
        if ((importTableInXML.equals(TABLENAME_VX9)) || (importTableInXML.equals(TABLENAME_VX10))) {
            postImportTaskForVX(factoryImportHelper);
        } else {
            postImportTaskForWX(factoryImportHelper);
            super.postImportTask();
        }
        colorTableDataExistsCache = null;
        colorTableContentIdExistsCache = null;
        referencedPartsCache = null;
        seriesAndPartToBCTEKeysCache = null;
        relevantUsagesOfColorTableCache = null;
        adatAsLongSet = null;
        processedVXDatasets = null;
        factoryImportHelper = null;
    }

    private void postImportTaskForWX(ColorTableFactoryImportHelper helper) {
        if (importToDB) {
            // Gesammelte PEM Stammdaten importieren/aktualisieren
            importPEMData();
            Iterator<Map.Entry<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>>> iterator = factoryDataWithSameSDATAMap.entrySet().iterator();
            while (iterator.hasNext()) {
                List<iPartsDataColorTableFactory> factoryDataInDBandImportList = new ArrayList<>();
                // Hole alle Werksdaten mit gleichen SDA unabhängig vom ADAT
                Map.Entry<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>> mapEntry = iterator.next();
                iPartsColorTableFactoryId factoryDatasIdWithoutADAT = mapEntry.getKey();
                Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> factoryDataInImportMap = mapEntry.getValue();
                // Set für Datensätze, die in der Versorgungsdatei vorkommen und von geladenen DB Datensätzen nicht
                // überschrieben werden dürfen
                Set<iPartsColorTableFactoryId> datasetsFromFile = new HashSet<>();
                for (WXDataObjectWithReferencedParts factoryDataWithParts : factoryDataInImportMap.values()) {
                    iPartsDataColorTableFactory factoryDataFromFile = factoryDataWithParts.getColorTableFactory();
                    datasetsFromFile.add(factoryDataFromFile.getAsId());
                    factoryDataInDBandImportList.add(factoryDataFromFile);
                }
                // Lade zu den importiereten Daten die Werksdaten aus der Datenbak (ebenfalls mit gleichem SDA unabhängig vom ADAT)
                iPartsDataColorTableFactoryList colorTableFactoryList = iPartsDataColorTableFactoryList.loadAllColorTableFactoryForColorTableFactoryId(getProject(), factoryDatasIdWithoutADAT);
                for (iPartsDataColorTableFactory datasetFromDB : colorTableFactoryList) {
                    // Nur aufnehmen, wenn der gleiche Datensatz in der Versorgungsdatei nicht vorkommt
                    if (!datasetsFromFile.contains(datasetFromDB.getAsId())) {
                        factoryDataInDBandImportList.add(datasetFromDB);
                    }
                }

                // Hole alle importierten Datensätze, die ein internes Löschkennzeichen haben
                Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> dataWithDeleteFlag = factoryDataWithDeletedFlag.remove(factoryDatasIdWithoutADAT);
                // Durchlaufe alle Datensätze mit Löschkennzeichen und überprüfe, ob bei den importierten und bestehenden
                // (in DB) Daten der gleiche Datensatz auch ohne Löschkennzeichen vorkommt. Falls ja, wird an dem Datensatz
                // das Löschkennzeichen gesetzt.
                if (dataWithDeleteFlag != null) {
                    for (iPartsDataColorTableFactory factoryData : factoryDataInDBandImportList) {
                        if (dataWithDeleteFlag.isEmpty()) {
                            break;
                        }
                        WXDataObjectWithReferencedParts factoryDataWithDeleteFlag = dataWithDeleteFlag.remove(factoryData.getAsId());
                        if (factoryDataWithDeleteFlag != null) {
                            factoryData.setFieldValueAsBoolean(FIELD_DCCF_IS_DELETED, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    // Existieren Datensätze mit Löschkennzeichen, die keinen bestehenden oder importierten Datensatz treffen,
                    // dann speicher den Datensatz mit Löschkennzeichen als eigenständigen Datensatz
                    if (!dataWithDeleteFlag.isEmpty()) {
                        for (WXDataObjectWithReferencedParts factoryDataWithDelete : dataWithDeleteFlag.values()) {
                            factoryDataInDBandImportList.add(factoryDataWithDelete.getColorTableFactory());
                        }
                    }
                }
                sortAndStoreFactoryDataForIDWithoutADAT(factoryDataInDBandImportList, factoryDatasIdWithoutADAT.getTableId(), dataWithDeleteFlag, factoryDataInImportMap, helper);
                // Eintrag entfernen
                iterator.remove();
            }
            // Alle übrig gebliebenen Löschdatensätze durchlaufen und mit den DB Daten vergleichen. Gibt es keinen passenden
            // DB Eintrag, dann wird der Datensatz als eigenständiger Datensatz angelegt.
            for (Map.Entry<iPartsColorTableFactoryId, Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts>> deletedEntries : factoryDataWithDeletedFlag.entrySet()) {
                // Lade zu den importiereten Daten die Werksdaten aus der Datenbak (ebenfalls mit gleichem SDA unabhängig vom ADAT)
                List<iPartsDataColorTableFactory> factoryList = new ArrayList<>(iPartsDataColorTableFactoryList.loadAllColorTableFactoryForColorTableFactoryId(getProject(), deletedEntries.getKey()).getAsList());
                Map<iPartsColorTableFactoryId, iPartsDataColorTableFactory> dbEntries = new HashMap<>();
                for (iPartsDataColorTableFactory colorFactoryDB : factoryList) {
                    dbEntries.put(colorFactoryDB.getAsId(), colorFactoryDB);
                }
                for (Map.Entry<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> deletedEntry : deletedEntries.getValue().entrySet()) {
                    iPartsDataColorTableFactory deletedFactoryData = dbEntries.get(deletedEntry.getKey());
                    if (deletedFactoryData != null) {
                        // Kommt der Datensatz in der DB vor, dann wird nur das interne Löschkennzeichen gesetzt
                        deletedFactoryData.setFieldValueAsBoolean(FIELD_DCCF_IS_DELETED, true, DBActionOrigin.FROM_EDIT);
                    } else {
                        // Kommt der Datensatz in der DB nicht vor, wird der Datenatz als eigenständiger Datensatz aufgenommen
                        factoryList.add(deletedEntry.getValue().getColorTableFactory());
                    }
                }
                sortAndStoreFactoryDataForIDWithoutADAT(factoryList, deletedEntries.getKey().getTableId(), null, deletedEntries.getValue(), helper);
            }
        }
    }

    /**
     * Sortiert alle Werksdaten nach ihrem Original-SDA (eventuell auch anch ADAT) und setzt die richtigen Zustände.
     * Am Ende werden die Datensätze gespeichert.
     *
     * @param factoryDataInDBandImportList
     * @param tableId
     * @param dataWithDeleteFlag
     * @param factoryDataInImportMap
     * @param helper
     */
    private void sortAndStoreFactoryDataForIDWithoutADAT(List<iPartsDataColorTableFactory> factoryDataInDBandImportList, String tableId,
                                                         Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> dataWithDeleteFlag,
                                                         Map<iPartsColorTableFactoryId, WXDataObjectWithReferencedParts> factoryDataInImportMap,
                                                         ColorTableFactoryImportHelper helper) {
        // Sortiere nun alle bestehenden und importierten Datensätze absteigend nach ihrem Original-SDA (das normale SDA ist
        // ja bei allen gleich, weil wir die Datensätze nach Werksdaten-ID ohne ADAT gruppiert haben).
        // Sollte das Original-SDA gleich sein, wird absteigend nach höchstem ADAT sortiert.
        FactoryDataHelper.sortProductionColorTableFactoryData(factoryDataInDBandImportList);

        String referencedSeriesNumber = ColorTableHelper.extractSeriesNumberFromTableId(tableId);
        iPartsDataReleaseState releaseStateFromEarliestDataset = null; // Status des jüngsten Datensatzes (in DB oder Versorgung)
        // Durchlaufe alle Datensätze und setze die richtigen Status
        for (iPartsDataColorTableFactory dataFactory : factoryDataInDBandImportList) {
            // Wurde schon ein Datensatz mit dem Status "neu" oder "freigegeben" gefunden und gespeichert?
            // Falls ja, muss der aktuelle Datensatz auf "nicht relevant" gesetzt werden
            if (releaseStateFromEarliestDataset != null) {
                // Ein jüngster Datensatz mit dem Status "neu" oder "freigegeben" wurde gefunden
                // -> Überprüfe, ob der aktuelle Datensatz auf "nicht relevant" gesetzt werden darf
                boolean isFromDB = ((dataWithDeleteFlag == null) || !dataWithDeleteFlag.containsKey(dataFactory.getAsId()))
                                   && !factoryDataInImportMap.containsKey(dataFactory.getAsId());
                // Überprüfung, ob es sich um einen Datensatz aus der DB oder der Versorgung handelt
                if (isFromDB) {
                    // Es muss beachtet werden, dass DB Datensätze mit dem Status "neu" nur geändert werden dürfen,
                    // wenn sich bisher kein Autor den Datensatz angeschaut hat.
                    if (dataFactory.getReleaseState() == iPartsDataReleaseState.NEW) {
                        String dataObjectId = dataFactory.getAsId().toDBString();
                        iPartsDataDIALOGChangeList dialogChanges = iPartsDataDIALOGChangeList.loadDIALOGChangesForDataObject(dataObjectId, getProject());
                        boolean isNotRelevant = true;
                        if (!dialogChanges.isEmpty()) {
                            for (iPartsDataDIALOGChange dialogChange : dialogChanges) {
                                if (!dialogChange.getFieldValue(FIELD_DDC_CHANGE_SET_GUID).isEmpty()) {
                                    isNotRelevant = false;
                                    break;
                                }
                            }
                        }
                        // Hat sich ein Autor den Datensatz schon angeschaut, dann kann man ihn nicht mehr auf
                        // "nicht relevant" setzen -> Überspringe den Datensatz
                        if (!isNotRelevant) {
                            continue;
                        }
                        dialogChanges.deleteFromDB(getProject(), true);
                    }
                    // Datensätze aus der DB dürfen nur auf "nicht relevant gesetzt werden, wenn
                    // - der jüngste gefundene Datensatz den Status "freigegeben" hat (es soll immer nur einen
                    // freigegebenen Datensatz geben)
                    // - der jüngste gefundene Datensatz den Status "neu" hat und der DB Datensatz einen Status
                    // ungleich "freigegeben" besitzt.
                    if ((releaseStateFromEarliestDataset == iPartsDataReleaseState.RELEASED)
                        || ((releaseStateFromEarliestDataset == iPartsDataReleaseState.NEW) && (dataFactory.getReleaseState() != iPartsDataReleaseState.RELEASED))) {
                        DIALOGImportHelper.setNotRelevantStateIfAllowed(dataFactory, FIELD_DCCF_STATUS);
                    }
                } else {
                    // Ältere Einträge in der Versorgung sollen auf "nicht relevant" gesetzt werden, wenn schon ein
                    // neuer oder freigegebener Datensatz gefunden wurde.
                    DIALOGImportHelper.setNotRelevantStateIfAllowed(dataFactory, FIELD_DCCF_STATUS);
                }
            }
            Set<String> referencedNumbers = null;
            WXDataObjectWithReferencedParts importedData = factoryDataInImportMap.get(dataFactory.getAsId());
            if (importedData != null) {
                referencedNumbers = importedData.getReferencedPartsForFatoryData();
            }
            // Datensatz mit seinem Status speichern
            saveDataObjects(helper, referencedSeriesNumber, referencedNumbers, dataFactory);
            if ((releaseStateFromEarliestDataset == null) && dataFactory.getReleaseState().isReleasedOrNew()) {
                releaseStateFromEarliestDataset = dataFactory.getReleaseState();
            }
        }
    }

    private void saveDataObjects(ColorTableFactoryImportHelper factoryImportHelper, String referencedSeriesNumber, Set<String> referencedPartNumbers,
                                 iPartsDataColorTableFactory colorTableFactory) {
        if (importToDB) {
            if ((colorTableFactory.getReleaseState() == iPartsDataReleaseState.NEW) && (referencedPartNumbers != null)) {
                // Änderungssatz für Kennzeichnung der Änderung in Stückliste (Änderung -> BCTE-Schlüssel) falls Status nicht RELEASED
                for (String referencedPartNumber : referencedPartNumbers) {
                    iPartsDataDIALOGChange dataDialogChange = factoryImportHelper.createDialogChange(colorTableFactory.getAsId(), referencedSeriesNumber, referencedPartNumber);
                    saveToDB(dataDialogChange);
                }
            }
            saveToDB(colorTableFactory);
        }
    }

    private void postImportTaskForVX(ColorTableFactoryImportHelper helper) {
        super.postImportTask();
        int nDatasetsMarkedForDeletion = 0;
        GenericEtkDataObjectList<iPartsDataDIALOGChange> dialogChangesDeleteList = new GenericEtkDataObjectList<>();
        if (!isCancelled()) {
            for (VXDataObjectForIdWithoutAdat vxData : processedVXDatasets.values()) {
                if (vxData.getImportDeletionDataObject() != null) {
                    nDatasetsMarkedForDeletion++;
                }

                if (importToDB) {
                    vxData.saveImportDataObjects(this, helper);
                }
            }
            getMessageLog().fireMessage(translateForLog("!!Lege %1 Löschdatensätze an", String.valueOf(nDatasetsMarkedForDeletion)));
            if (importToDB) {
                for (VXDataObjectForIdWithoutAdat vxData : processedVXDatasets.values()) {
                    vxData.saveDeletionDataObject(this, helper, dialogChangesDeleteList);
                }
            }
            getMessageLog().fireMessage(translateForLog("!!Es wurden %1 Löschdatensätze angelegt", String.valueOf(nDatasetsMarkedForDeletion)));
        }
        super.postImportTask();
        if (!isCancelled() && importToDB) {
            // Löschen der DIALOG_CHANGES erst ganz zum Schluss
            dialogChangesDeleteList.deleteFromDB(getProject(), true);
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(dbTableName)) {
            getProject().getDB().delete(dbTableName, new String[]{ FIELD_DCCF_DATA_ID, FIELD_DCCF_SOURCE }, new String[]{ currentDatasetId.getDbValue(), iPartsImportDataOrigin.DIALOG.getOrigin() });
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(dbTableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /**
     * Setzt den aktuellen Datensatzkenner und den dazugehörigen Datensatzprefix
     *
     * @param currentDataBaseId
     */
    public void setCurrentDataBaseId(iPartsFactoryDataTypes currentDataBaseId) {
        if (currentDataBaseId != null) {
            this.currentDatasetId = currentDataBaseId;
            prefixForImporterInstance = currentDatasetId.getDatasetValue() + "_";
        }
    }

    @Override
    protected String getSTCFieldname(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(STCA);
        } else {
            return getSourceFieldName(STCB);
        }
    }

    @Override
    protected String getPEMTFieldname(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(PEMTA);
        } else {
            return getSourceFieldName(PEMTB);
        }
    }

    @Override
    protected String getPemFieldName(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(PEMA);
        } else {
            return getSourceFieldName(PEMB);
        }
    }

    @Override
    protected String getFactoryFieldname() {
        return getSourceFieldName(WK);
    }

    @Override
    protected String getADATFieldname() {
        return getSourceFieldName(ADAT);
    }

    private class ColorTableFactoryImportHelper extends DIALOGImportHelper {

        public ColorTableFactoryImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(prefixForImporterInstance + TEIL) || sourceField.equals(prefixForImporterInstance + FT)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(prefixForImporterInstance + SDA) || sourceField.equals(prefixForImporterInstance + SDB)
                       || sourceField.equals(prefixForImporterInstance + ADAT)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(getSourceFieldName(PEMA)) || sourceField.equals(prefixForImporterInstance + PEMB)
                       || sourceField.equals(getSourceFieldName(STCA)) || sourceField.equals(prefixForImporterInstance + STCB)) {
                value = value.trim();
            } else if (sourceField.equals(getSourceFieldName(PEMTA)) || sourceField.equals(prefixForImporterInstance + PEMTB)) {
                value = getDIALOGDateValueForPEMT(value);
            }
            return value;
        }

        public String getColorTableId(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + FT, importRec);
        }

        public String getFactoryXxP(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + WK, importRec);
        }

        public String getFactoryVXx(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + WERK, importRec);
        }

        public String getSData(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + SDA, importRec);
        }

        public String getPos(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + POS, importRec);
        }

        public String getFikz(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + FIKZ, importRec);
        }

        public String getADat(Map<String, String> importRec) {
            return handleValueOfSpecialField(prefixForImporterInstance + ADAT, importRec);
        }

        private iPartsDataDIALOGChange createDialogChange(iPartsColorTableFactoryId colorTableFactoryId, String seriesNo, String matNr) {
            return createChangeRecord(iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA,
                                      colorTableFactoryId, seriesNo, "", matNr, "");
        }

        private String getIdForBCTEPrimaryKeys(String seriesNo, String matNr) {
            return seriesNo + "|" + matNr;
        }

    }

    private static class RelevantUsagesOfColorTable {

        private final Set<String> relevantPartNumbers;
        private final Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys;

        private RelevantUsagesOfColorTable(Set<String> relevantPartNumbers, Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys) {
            this.relevantPartNumbers = relevantPartNumbers;
            this.relevantBCTEPrimaryKeys = relevantBCTEPrimaryKeys;
        }

        private Set<String> getRelevantPartNumbers() {
            return relevantPartNumbers;
        }

        private Set<iPartsDialogBCTEPrimaryKey> getRelevantBCTEPrimaryKeys() {
            return relevantBCTEPrimaryKeys;
        }
    }

    /**
     * Hilfsklasse für VX9 und VX10 Datensätze zu einer ID ohne Adat. Enthält also alle Änderungstände für eine ID ohne das ADAT der ID
     */
    private static class VXDataObjectForIdWithoutAdat extends ImportDataObjectsForPartialId<iPartsDataColorTableFactory> {

        @Override
        public Comparator<iPartsDataColorTableFactory> getComparator() {
            return iPartsDataColorTableFactory.adatComparator;
        }

        @Override
        public String getStatusField() {
            return FIELD_DCCF_STATUS;
        }

        @Override
        public String getSourceField() {
            return FIELD_DCCF_SOURCE;
        }

        @Override
        public String getAdatField() {
            return null;
        }
    }

    /**
     * Hilfsklasse für Produktionsdaten mit unterschiedlichen Original-SDAs und gleichen Entwicklungs-SDAs
     */
    private static class WXDataObjectWithReferencedParts {

        private final iPartsDataColorTableFactory colorTableFactory;
        private final Set<String> referencedPartsForFatoryData;

        public WXDataObjectWithReferencedParts(iPartsDataColorTableFactory colorTableFactory, Set<String> referencedPartsForFatoryData) {
            this.colorTableFactory = colorTableFactory;
            this.referencedPartsForFatoryData = referencedPartsForFatoryData;
        }

        public iPartsDataColorTableFactory getColorTableFactory() {
            return colorTableFactory;
        }

        public Set<String> getReferencedPartsForFatoryData() {
            return referencedPartsForFatoryData;
        }
    }

}