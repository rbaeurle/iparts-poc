package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.ppua;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordCSVFileReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordGzCSVFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPPUA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPPUAList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPPUAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.util.*;

/**
 * PPUA (PartsPotentialUsageAnalysis) Differenz-Importer
 */
public class PPUAImporter extends AbstractListComparerDataImporter {

    private static final String PARTNO = "Partnr";
    private static final String REGION = "Region";
    private static final String ENTITY = "Entity";
    private static final String VEHBR = "VehBR";
    private static final String TYPE = "Type";

    private static final String[] MUST_HEADERNAMES = new String[]{ PARTNO, REGION, ENTITY, VEHBR, TYPE };

    private Set<String> yearHeaderSet;
    private int insertedCount;

    public PPUAImporter(EtkProject project) {
        super(project, iPartsConst.PPUA_POTENTIAL_USAGE_ANALYSIS, iPartsConst.TABLE_DA_PPUA, true,
              new FilesImporterFileListType(iPartsConst.TABLE_DA_PPUA, iPartsConst.PPUA_POTENTIAL_USAGE_ANALYSIS, true,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void preImportTask() {
        // WICHTIG: für eventuelle Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        setMaxEntriesForCommit(MIN_MEMORY_ROWS_LIMIT);
        setCheckForDoubles(false);
        createComparer(150, 50);
        if (saveToDB) {
            loadExistingDataFromDB();
        }
        if (totalDBCount == 0) {
            fireMessage("!!Erstbefüllung");
            clearEndMessageList();
            insertedCount = 0;
        } else {
            fireMessage("!!Einlesen der Daten aus Datei");
        }
    }

    @Override
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataPPUA data = new iPartsDataPPUA(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    protected String getValuesForListComp(EtkDataObject data) {
        if (data instanceof iPartsDataPPUA) {
            return ((iPartsDataPPUA)data).getYearValueToDbString();
        }
        return "";
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataPPUAList();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        PPUAImportHelper importHelper = new PPUAImportHelper(getProject(), null, destTable);
        iPartsPPUAId basicId = importHelper.buildBaseIdFromImportRecord(importRec, recordNo);
        if (basicId.isValidId()) {
            for (String yearName : yearHeaderSet) {
                iPartsPPUAId ppuaId = new iPartsPPUAId(basicId, yearName.trim());
                if (totalDBCount > 0) {
                    putSecond(ppuaId.toDBString(), importHelper.getValuesFromImport(yearName, importRec, recordNo));
                } else {
                    iPartsDataPPUA dataPPUA = new iPartsDataPPUA(getProject(), ppuaId);
                    dataPPUA.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    dataPPUA.setYearValueFromDbString(importHelper.getValuesFromImport(yearName, importRec, recordNo), DBActionOrigin.FROM_EDIT);
                    doSaveToDB(dataPPUA);
                    insertedCount++;
                }
            }
        } else {
            // Warning
            fireWarning("!!In Zeile %1: Ungültige Id %2. Wird ignoriert!", String.valueOf(recordNo), basicId.toStringForLogMessages());
            increaseSkippedRecord();
        }
    }

    @Override
    protected void postImportTask() {
        if (!cancelled) {
            if (saveToDB) {
                if (totalDBCount > 0) {
                    compareAndSaveData(false);
                } else {
                    addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(insertedCount));
                }
            }
        }
        cleanup();
        super.postImportTask();
    }

    protected void logImportRecordsFinished(int importRecordCount) {
        if (isSingleCall) {
            if (skippedRecords > 0) {
                fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                            getDatasetTextForLog(importRecordCount)) +
                            ", " + translateForLog("!!%1 %2 übersprungen", String.valueOf(skippedRecords),
                                                   getDatasetTextForLog(skippedRecords)));
            } else {
                fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                            getDatasetTextForLog(importRecordCount)));
            }
            showEndMessage(true);
        } else {
            super.logImportRecordsFinished(importRecordCount);
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(destTable)) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(preparePPUAImporterKeyValueGZ(importFile, destTable, '\t', withHeader, null, Character.MIN_VALUE, DWFileCoding.CP_1252));
            } else {
                return importMasterData(preparePPUAImporterKeyValue(importFile, destTable, '\t', withHeader, null, Character.MIN_VALUE, DWFileCoding.CP_1252));
            }
        }
        return false;
    }

    @Override
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        iPartsPPUAId mappingId = iPartsPPUAId.getFromDBString(entry.getKey());
        if (mappingId != null) {
            return new iPartsDataPPUA(getProject(), mappingId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        if (data instanceof iPartsDataPPUA) {
            ((iPartsDataPPUA)data).setYearValueFromDbString(entry.getValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    private boolean checkHeader(Map<String, Integer> headerNameToIndex) {
        yearHeaderSet = new TreeSet<>();
        for (String headerName : MUST_HEADERNAMES) {
            if (headerNameToIndex.get(headerName) == null) {
                // Error missing HeaderName
                return false;
            }
        }
        for (String headerName : headerNameToIndex.keySet()) {
            String testHeaderName = headerName.trim();
            if (StrUtils.isInteger(testHeaderName)) {
                int headerValue = StrUtils.strToIntDef(testHeaderName, -1);
                if (headerValue >= 1995) {
                    yearHeaderSet.add(headerName);
                }
            }
        }
        return !yearHeaderSet.isEmpty();
    }

    private AbstractKeyValueRecordReader preparePPUAImporterKeyValueGZ(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                       String[] headerNames, char quoteSign, DWFileCoding encoding) {
        PPUAKeyValueRecordGzCSVFileReader reader = new PPUAKeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames, encoding);
        reader.setQuoteSign(quoteSign);
        reader.setSeparator(separator);
        return reader;
    }

    protected AbstractKeyValueRecordReader preparePPUAImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                       String[] headerNames, char quoteSign, DWFileCoding encoding) {
        PPUAKeyValueRecordCSVFileReader reader = new PPUAKeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames, separator, encoding);
        reader.setQuoteSign(quoteSign);
        return reader;
    }


    private class PPUAKeyValueRecordGzCSVFileReader extends KeyValueRecordGzCSVFileReader {

        public PPUAKeyValueRecordGzCSVFileReader(DWFile file, String tableName, boolean withHeader, String[] headerNames, DWFileCoding encoding) {
            super(file, tableName, withHeader, headerNames, encoding);
        }

        @Override
        protected void handleHeaderLine() {
            super.handleHeaderLine();
            if (!checkHeader(getHeaderNameToIndex())) {
                cancelImport();
            }
        }
    }

    private class PPUAKeyValueRecordCSVFileReader extends KeyValueRecordCSVFileReader {

        public PPUAKeyValueRecordCSVFileReader(DWFile file, String tableName, boolean withHeader, String[] headerNames, char separator, DWFileCoding encoding) {
            super(file, tableName, withHeader, headerNames, separator, encoding);
        }

        @Override
        protected void handleHeaderLine() {
            super.handleHeaderLine();
            if (!checkHeader(getHeaderNameToIndex())) {
                cancelImport();
            }
        }
    }

    private class PPUAImportHelper extends MADImportHelper {

        public PPUAImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsPPUAId buildBaseIdFromImportRecord(Map<String, String> importRec, int recordNo) {
            return new iPartsPPUAId(handleValueOfSpecialField(PARTNO, importRec), handleValueOfSpecialField(REGION, importRec),
                                    handleValueOfSpecialField(VEHBR, importRec), handleValueOfSpecialField(ENTITY, importRec),
                                    handleValueOfSpecialField(TYPE, importRec), "");
        }

        public String getValuesFromImport(String yearName, Map<String, String> importRec, int recordNo) {
            return handleValueOfSpecialField(yearName, importRec);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = StrUtils.getEmptyOrValidString(value);
            if (sourceField.equals(VEHBR)) {
                if (StrUtils.isValid(value) && !value.startsWith(MODEL_NUMBER_PREFIX_CAR)) {
                    value = MODEL_NUMBER_PREFIX_CAR + value;
                }
            }
            return value;
        }
    }
}
