/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsVS2USData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsVS2USDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import org.apache.commons.collections4.map.LRUMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die Zuordnung zwischen Fahrzeug- und Aggregatebaureihen (X6E/Y6E)
 */
public class ModuleRelationImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    private static final String TABLENAME_PREFIX = "T10R";
    private static final String X6E_PREFIX = "X6E";
    private static final String Y6E_PREFIX = "Y6E";
    public static final String IMPORT_TABLENAME_X6E = TABLENAME_PREFIX + X6E_PREFIX;
    public static final String IMPORT_TABLENAME_Y6E = TABLENAME_PREFIX + Y6E_PREFIX;

    // Die Zieltabelle (ist in iPartsConst definiert)
    private static final String DEST_TABLENAME = TABLE_DA_VS2US_RELATION;

    // Die XML-Elemente
    private static final String FBR = "FBR";
    private static final String POS = "POS";
    private static final String PV = "PV";
    private static final String AA = "AA";
    private static final String ABRAA = "ABRAA";
    private static final String SDA = "SDA";
    private static final String SDB = "SDB";
    private static final String GRP = "GRP";
    private static final String LK = "LK";
    private static final String RFG = "RFG";
    private static final String MG = "MG";
    private static final String VERT = "VERT";
    private static final String FED = "FED";
    private static final String PGKZ = "PGKZ";
    private static final String CBED = "CBED";
    private static final String EREIA = "EREIA";
    private static final String EREIB = "EREIB";

    private HashMap<String, String> mapping;
    private HashMap<String, String> seriesMapping;
    private String importTableInXML;
    private String[] primaryKeysForImportData;
    private String prefixForImporterInstance;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;
    private Map<IdWithType, Boolean> seriesTableDataExistsCache;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ModuleRelationImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";
        importName = "!!DIALOG Zuordnung Fahrzeugbaureihe zur Aggregatebaureihe";
        // Unterscheidung X6E - Y6E
        if (importTableInXML.equals(IMPORT_TABLENAME_X6E)) {
            prefixForImporterInstance = X6E_PREFIX + "_";
            nameForImport = DD_VS2US_NAME;
            importName += " (" + X6E_PREFIX + ")";
        } else if (importTableInXML.equals(IMPORT_TABLENAME_Y6E)) {
            prefixForImporterInstance = Y6E_PREFIX + "_";
            nameForImport = DD_VS2US_NAME_EVENT;
            importName += " (" + Y6E_PREFIX + ")";
            mapping.put(FIELD_VUR_EVENT_FROM, prefixForImporterInstance + EREIA);     // nur Y6E
            mapping.put(FIELD_VUR_EVENT_TO, prefixForImporterInstance + EREIB);     // nur Y6E
        }
        // Die Primärschlüsselfelder, wie sie in den zu importierenden Daten (die Namen der Import-Spalten) existieren müssen:
        primaryKeysForImportData = new String[]{ prefixForImporterInstance + FBR, prefixForImporterInstance + POS,
                                                 prefixForImporterInstance + PV, prefixForImporterInstance + AA,
                                                 prefixForImporterInstance + ABRAA, prefixForImporterInstance + SDA };

        // DB-Spalte auf XML-Element (Schlüssel-Felder werden via ID Objekt gemappt)
        mapping.put(FIELD_VUR_DATB, prefixForImporterInstance + SDB);
        mapping.put(FIELD_VUR_GROUP, prefixForImporterInstance + GRP);
        mapping.put(FIELD_VUR_STEERING, prefixForImporterInstance + LK);
        mapping.put(FIELD_VUR_RFG, prefixForImporterInstance + RFG);
        mapping.put(FIELD_VUR_QUANTITY, prefixForImporterInstance + MG);
        mapping.put(FIELD_VUR_DISTR, prefixForImporterInstance + VERT);
        mapping.put(FIELD_VUR_FED, prefixForImporterInstance + FED);
        mapping.put(FIELD_VUR_PRODUCT_GRP, prefixForImporterInstance + PGKZ);
        mapping.put(FIELD_VUR_CODES, prefixForImporterInstance + CBED);

        // Mapping für den Eintrag in DA_SERIES
        seriesMapping = new HashMap<>();
        seriesMapping.put(FIELD_DS_NAME, prefixForImporterInstance + ABRAA);
        seriesMapping.put(FIELD_DS_SDATA, prefixForImporterInstance + SDA);
        seriesMapping.put(FIELD_DS_SDATB, prefixForImporterInstance + SDB);
        seriesMapping.put(FIELD_DS_PRODUCT_GRP, prefixForImporterInstance + PGKZ);

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(DEST_TABLENAME,
                                                                                         nameForImport, false, false, false,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // Folgende Felder müssen existieren und gefüllt sein: Primärschlüsselfelder, Änderungsstand, KEM und Freigabe, jeweils von bis
        String[] mustExists = StrUtils.mergeArrays(primaryKeysForImportData, new String[]{ prefixForImporterInstance + SDB });
        String[] mustHaveData = StrUtils.mergeArrays(primaryKeysForImportData, new String[]{ prefixForImporterInstance + SDB });
        // An den Importer anhängen.
        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME_X6E) ||
                   importer.getTableNames().get(0).equals(IMPORT_TABLENAME_Y6E);
        }
        return false;

    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
        // WICHTIG: für die Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
        seriesTableDataExistsCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
    }

    /**
     * Die Importlogik:
     * - Nur in der Datenbank als "versorgungsrelevant" gekennzeichnete Fahrzeugbaureihen importieren.
     * - Aggregatebaureihen von "NICHT versorgungsrelevant" gekennzeichneten Fahrzeugbaureihen überspringen.
     * - Eine Prüfung der Aggregatebaureihe findet nicht statt.
     * - Nicht in der Datenbank enthaltene Baureihen werden als "nicht versorgungsrelevant" interpretiert.
     * - SDA + SDB, [020090724145743], führende '0' abschneiden
     * - SDB [999999999999999] als 'unendlich' interpretieren.
     * - Anzeigeformat und Speicherformat der Fahrzeugbaureihe unterscheiden und korrigieren
     * - Anzeigeformat und Speicherformat der Aggregatebaureihe unterscheiden und korrigieren
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        RelationImportHelper importHelper = new RelationImportHelper(getProject(), mapping, DEST_TABLENAME);
        // Einmalig am Anfang die Speicherformate umwandeln:
        // Unterscheidung Speicherformat(="02C204         C") und Anzeigeformat(="C204")
        String modFahrzeugBaureihe = importHelper.handleValueOfSpecialField(prefixForImporterInstance + FBR, importRec);
        // Unterscheidung Speicherformat(="03D2719        D") und Anzeigeformat(="D2719")
        //                Speicherformat(="03D2719x       D") und Anzeigeformat(="D2719x")
        String modAggregatBaureihe = importHelper.handleValueOfSpecialField(prefixForImporterInstance + ABRAA, importRec);

        // Wenn die FAHRZEUG-Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(modFahrzeugBaureihe, getInvalidSeriesSet(), this)) {
            return;
        }

        // Überprüfen, ob die Fahrzeugbaureihe der Aggregatebaureihe bereits als Baureihenstammdatensatz existiert.
        // Wenn nicht, den Datensatz mit Warnung trotzdem importieren
        iPartsSeriesId seriesId = new iPartsSeriesId(modFahrzeugBaureihe);
        iPartsDataSeries dataSeries = new iPartsDataSeries(getProject(), seriesId);
        if (!seriesTableEntryExists(seriesId, dataSeries)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig (Fahrzeugbaureihe %2 existiert nicht.)",
                                                        String.valueOf(recordNo), modFahrzeugBaureihe),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }

        // Überprüfung, ob die Aggregatebaureihe bereits in den Baureihenstammdaten vorhanden ist.
        // Wenn nicht, eine neue Aggregatebaureihe in [DA_SERIES] anlegen.
        seriesId = new iPartsSeriesId(modAggregatBaureihe);
        dataSeries = new iPartsDataSeries(getProject(), seriesId);

        if (!dataSeries.loadFromDB(seriesId)) {
            // [T10RBRS] = [DA_SERIES]
            dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            RelationImportHelper seriesImportHelper = new RelationImportHelper(getProject(), seriesMapping, TABLE_DA_SERIES);
            seriesImportHelper.fillOverrideCompleteDataForDIALOGReverse(dataSeries, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
            // Neuen Aggregatebaureihenstammdatensatz speichern
            if (importToDB) {
                if (!saveToDB(dataSeries)) {
                    skippedRecords--;
                }
            }
        }

        // Die Zuordnung Fahrzeugbaureihe zu Aggregatebaureihe übernehmen.
        iPartsVS2USDataId id = importHelper.buildiPartsVS2USDataId(importRec, modFahrzeugBaureihe, modAggregatBaureihe);
        iPartsVS2USData importData = new iPartsVS2USData(getProject(), id);
        if (!importData.existsInDB()) {
            importData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        importHelper.fillOverrideCompleteDataForDIALOGReverse(importData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        // Neue Zuordnung speichern.
        if (importToDB) {
            saveToDB(importData);
        }

    }

    private boolean seriesTableEntryExists(iPartsSeriesId seriesId, iPartsDataSeries dataSeries) {
        return existsInDBWithCache(seriesTableDataExistsCache, seriesId, dataSeries);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        seriesTableDataExistsCache = null;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            getProject().getDB().delete(DEST_TABLENAME);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class RelationImportHelper extends DIALOGImportHelper {

        public RelationImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsVS2USDataId buildiPartsVS2USDataId(Map<String, String> importRec, String modFahrzeugBaureihe, String modAggregatBaureihe) {
            return new iPartsVS2USDataId(modFahrzeugBaureihe,
                                         handleValueOfSpecialField(prefixForImporterInstance + POS, importRec),
                                         handleValueOfSpecialField(prefixForImporterInstance + PV, importRec),
                                         handleValueOfSpecialField(prefixForImporterInstance + AA, importRec),
                                         modAggregatBaureihe,
                                         handleValueOfSpecialField(prefixForImporterInstance + SDA, importRec));
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(prefixForImporterInstance + FBR) || sourceField.equals(prefixForImporterInstance + ABRAA)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(prefixForImporterInstance + SDA) || sourceField.equals(prefixForImporterInstance + SDB)) {
                value = getDIALOGDateTimeValue(value);
            }
            return value;
        }

    }

}