/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSupplierPartNoMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSupplierPartNoMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSupplierPartNoMappingId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.file.DWFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;

/**
 * Importer für die Zuordnung von Lieferantensachnummern zu Daimler-A-Sachnummern aus SRM
 */
public class SrmSupplierPartNoMappingDataImporter extends AbstractListComparerDataImporter implements iPartsConst, EtkDbConst {

    private static final int NEEDED_LINE_LENGTH = 82;

    private static final String TYPE = "SrmSupplierPartNoMappingImportType";

    public SrmSupplierPartNoMappingDataImporter(EtkProject project) {
        super(project, SRM_SUPPLIERPARTNO_MAPPING, TABLE_DA_SUPPLIER_PARTNO_MAPPING, false,
              new FilesImporterFileListType(TYPE, SRM_SUPPLIERPARTNO_MAPPING,
                                            true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_TXT }));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Keine Records, die Verarbeitung findet in importFile() statt.
    }

    @Override
    public boolean isAutoImport() {
        return true;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {

        if (!importFile.isFile()) {
            fireError("!!Invalid import file %1", importFile.getName());
            return false;
        }

        createComparer(100, 20);
        try {
            // Laden der Datenbankeinträge und speichern in der FirstList des Comparers
            if (loadExistingDataFromDB()) {
                // Importdatei lesen und dabei die SecondList des Compareres befüllen
                if (loadDataFromImportFile(importFile)) {
                    // Und die Veränderungen in der Datenbank speichern/löschen/anlegen
                    if (compareAndSaveData(true)) {
                        postImportTask(); // <<===== muss man hier manuell machen, sonst wird nichts in die DB geschrieben.
                    }
                }
            }
        } finally {
            cleanup();
        }
        return true;
    }

    @Override
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataSupplierPartNoMapping data = new iPartsDataSupplierPartNoMapping(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    @Override
    protected String getValuesForListComp(EtkDataObject data) {
        if (data instanceof iPartsDataSupplierPartNoMapping) {
            return ((iPartsDataSupplierPartNoMapping)data).getFieldValuesToDbString();
        }
        return "";
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataSupplierPartNoMappingList();
    }

    @Override
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        iPartsSupplierPartNoMappingId mappingId = iPartsSupplierPartNoMappingId.getFromDBString(entry.getKey());
        if (mappingId != null) {
            return new iPartsDataSupplierPartNoMapping(getProject(), mappingId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        if (data instanceof iPartsDataSupplierPartNoMapping) {
            ((iPartsDataSupplierPartNoMapping)data).setFieldValuesFromDbString(entry.getValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    public boolean doSaveToDB(EtkDataObject dataObject) {
        // Das Feld FIELD_DSPM_SUPPLIER_PARTNO_PLAIN setzen
        if (dataObject instanceof iPartsDataSupplierPartNoMapping) {
            iPartsDataSupplierPartNoMapping dataSupplierPartNoMapping = (iPartsDataSupplierPartNoMapping)dataObject;
            String supplierPartNoPlain = StrUtils.removeCharsFromString(dataSupplierPartNoMapping.getAsId().getSupplierPartNo(), new char[]{ ' ' });
            dataSupplierPartNoMapping.setFieldValue(FIELD_DSPM_SUPPLIER_PARTNO_PLAIN, supplierPartNoPlain, DBActionOrigin.FROM_EDIT);
        }

        return super.doSaveToDB(dataObject);
    }

    /**
     * Das ist die Liste der Datensätze aus der Importdatei.
     * Erzeuge alle Datensätze aus der Importdatei
     *
     * @param importFile
     * @return
     */
    private boolean loadDataFromImportFile(DWFile importFile) {

        SupplierPartNoReader reader = new SupplierPartNoReader();

        if (reader.open(importFile)) {
            try {
                fireMessage("!!Laden der Daten aus der Importdatei");
                int maxPos = MAX_MEMORY_ROWS_LIMIT;  // nur damit sich was bewegt
                String oneLine;
                String partNo = "";         // Daimler-Sachnummer

                while (!reader.isEOF()) {
                    if (isCancelled()) {
                        return false;
                    }
                    oneLine = reader.readLine();
                    if (StrUtils.isValid(oneLine)) {
                        if (oneLine.length() < NEEDED_LINE_LENGTH) {
                            fireWarning("!!Zeile: (%1) mit Länge %2 hat nicht die nötige Mindestlänge von %3 Zeichen und wird übersprungen! [\"%4\"]",
                                        String.valueOf(reader.getLineCounter()), String.valueOf(oneLine.length()),
                                        String.valueOf(NEEDED_LINE_LENGTH), oneLine);
                            continue;
                        }

                        /* Das sind die zwei verschiedenen Datensätze: ^01 und ^02
                         *
                         * Satzart 01 mit der A-SNR und
                         * Satzart 02 mit Lieferantensachnummer, Lieferantennummer und Lieferantenname
                         *
                         *  01A0000160434             OELABSCHEIDEREINSATZ
                         *  021 928 404 725                                     102/00020      BOSCH
                         *  0239 310 50 910                                     112/00011      MANN
                         */

                        if (isRecordType01(oneLine)) {   // Satzart (01)
                            partNo = getPartNo(oneLine);
                        } else if (isRecordType02(oneLine)) { // Satzart (02)
                            String supplierPartNo = getSupplierPartNo(oneLine);
                            String supplierNo = getSupplierNo(oneLine);
                            String supplierName = getSupplierName(oneLine);

                            // Nur im Falle eines (02)er Datensatzes könnte etwas geschrieben werden, wenn die Randbedingungen passen.
                            // Leere Lieferantensachnummern werden übersprungen.
                            if (!StrUtils.isValid(supplierPartNo)) {
                                fireMessage("!!In Zeile: (%1) %2 ist leer. Wird übersprungen!",
                                            String.valueOf(reader.getLineCounter()), "SupplierPartNumber");
                                continue;
                            }
                            // Datensätze mit A-SNR in Satzart 01 gleich Lieferantensachnummer in Satzart 02 werden übersprungen.
                            if (partNo.trim().equals(supplierPartNo.trim())) {
                                fireMessage("!!In Zeile: (%1) %2 sind gleich. Wird übersprungen!",
                                            String.valueOf(reader.getLineCounter()), "PartNumber/SupplierPartNumber");
                                continue;
                            }

                            if (StrUtils.isValid(partNo)) {
                                // In der zweiten Liste des Comparers speichern
                                iPartsSupplierPartNoMappingId id = new iPartsSupplierPartNoMappingId(partNo, supplierPartNo, supplierNo);
                                putSecond(id.toDBString(), supplierName);
                            } else {
                                fireWarning("!!In Zeile: (%1) SatzArt 02 ohne Satzart 01. Wird übersprungen!", String.valueOf(reader.getLineCounter()));
                            }
                        } else { // unbekannte Satzart
                            fireWarning("!!Unbekannte Satzart in Zeile: (%1)! (\"%2\"). Wird übersprungen!",
                                        String.valueOf(reader.getLineCounter()), oneLine);
                        }
                        int pos = (int)reader.getLineCounter();
                        updateProgress(pos, maxPos);
                    } else {
                        if (reader.hasErrors()) {
                            fireError("!!Fehler während des Imports in Zeile: (%1): %2",
                                      String.valueOf(reader.getLineCounter()), reader.getErrorMsg());
                            return false;
                        }
                    }
                }
            } finally {
                fireMessage("!!%1 Datensätze gelesen", String.valueOf(reader.getLineCounter()));
                reader.close();
                getMessageLog().hideProgress();
            }
        }
        return true;
    }

    /**
     * Holt die Daimler-A-Sachnummer aus einem (01)er Datensatz
     * <p>
     * -          1         2         3         4         5         6         7         8
     * -0123456789012345678901234567890123456789012345678901234567890123456789012345678901-
     * "01A0000160420             ZYLINDERKOPFDICHTUNG                                    "
     *
     * @param line
     * @return
     */

    private String getPartNo(String line) {
        return getFixedLengthValue(line, 2, 26);
    }


    /**
     * Holt die Lieferantenteilenummer aus einem (02)er Datensatz
     * <p>
     * -          1         2         3         4         5         6         7         8
     * -0123456789012345678901234567890123456789012345678901234567890123456789012345678901-
     * "021 928 404 725                                     102/00020      BOSCH          "
     * "0270490291                                                         MAXION         "
     * "020 433 271 313 / DLLA 140S 637                     102/00012      BOSCH          "
     *
     * @param line
     * @return
     */
    private String getSupplierPartNo(String line) {
        return getFixedLengthValue(line, 2, 52);
    }


    /**
     * Holt die Lieferantenummer aus einem (02)er Datensatz
     * <p>
     * -          1         2         3         4         5         6         7         8
     * -0123456789012345678901234567890123456789012345678901234567890123456789012345678901-
     * "021 928 404 725                                     102/00020      BOSCH          "
     * "0270490291                                                         MAXION         "
     * "020 433 271 313 / DLLA 140S 637                     102/00012      BOSCH          "
     *
     * @param line
     * @return
     */
    private String getSupplierNo(String line) {
        return getFixedLengthValue(line, 52, 67);
    }

    /**
     * Holt den Lieferantennamen aus einem (02)er Datensatz
     * <p>
     * -          1         2         3         4         5         6         7         8
     * -0123456789012345678901234567890123456789012345678901234567890123456789012345678901-
     * "021 928 404 725                                     102/00020      BOSCH          "
     * "0270490291                                                         MAXION         "
     * "020 433 271 313 / DLLA 140S 637                     102/00012      BOSCH          "
     *
     * @param line
     * @return
     */
    private String getSupplierName(String line) {
        return getFixedLengthValue(line, 67, 82);
    }

    private String getFixedLengthValue(String line, int beginIndex, int endIndex) {
        if (StrUtils.isEmpty(line)) {
            return "";
        }
        beginIndex = Math.max(beginIndex, 0);
        int len = line.length();
        if (beginIndex >= len) {
            return "";
        }
        if (beginIndex > endIndex) {
            return "";
        }
        if (endIndex >= len) {
            endIndex = len - 1;
        }
        return StrUtils.trimRight(line.substring(beginIndex, endIndex));
    }

    /**
     * Record Typ (01) ist die Daimler-A-Sachnummer + Beschreibung, die nicht importiert wird
     *
     * @param line
     * @return
     */
    private boolean isRecordType01(String line) {
        return isRecordType(line, "01");
    }

    /**
     * Record Typ (2) ist die Lieferanten-Sachnummer, Lieferantennummer + Lieferantenname
     * Es gibt viele Datensätze, bei denen die Lieferantennummer leer ist
     * und der Lieferantenname offensichtlich kein Lieferantenname ist.
     *
     * @param line
     * @return
     */
    private boolean isRecordType02(String line) {
        return isRecordType(line, "02");
    }

    private boolean isRecordType(String line, String recordPrefix) {
        if (StrUtils.isValid(line) && (line.startsWith(recordPrefix))) {
            return true;
        }
        return false;
    }

    /**
     * Lesen der Inputdatei über einen Stream in einer separaten Klasse.
     */
    private class SupplierPartNoReader {

        private FileInputStream inputStream;
        private Reader inputStreamReader;
        private BufferedReader bufferedReader;
        private boolean isEOF;
        private long lineCounter;
        private StringBuilder errorMsgs;


        public SupplierPartNoReader() {
            reset();
        }

        public boolean open(DWFile importFile) {
            boolean result = true;
            if (isInit()) {
                return false;
            }
            reset();
            try {
                inputStream = new FileInputStream(importFile.getAbsolutePath());
                inputStreamReader = new InputStreamReader(inputStream, Charset.forName("CP850")); // <<====== das kann sich nochmal ändern, je nach Codierung.
                bufferedReader = new BufferedReader(inputStreamReader);
            } catch (FileNotFoundException e) {
                addError(e.getMessage());
                internalClose();
                result = false;
            } catch (UnsupportedCharsetException x) {
                addError(x.getMessage());
                internalClose();
                result = false;
            }
            return result;
        }

        public boolean isInit() {
            return bufferedReader != null;
        }

        public String readLine() {
            if (!isInit()) {
                return null;
            }

            try {
                lineCounter++;
                String line = bufferedReader.readLine();
                if (line == null) {
                    // Es gibt keine Zeilen mehr
                    isEOF = true;
                    return null;
                }
                return line;
            } catch (IOException e) {
                addError(e.getMessage());
                return null;
            }
        }

        public long getLineCounter() {
            return lineCounter;
        }

        public boolean isEOF() {
            return isEOF;
        }

        public void close() {
            if (isInit()) {
                internalClose();
            }
        }

        private void internalClose() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
//                e.printStackTrace();
            } finally {
                reset();
            }
        }

        private void addError(String text) {
            if (hasErrors()) {
                errorMsgs.append("\n");
            }
            errorMsgs.append(text);
        }

        public boolean hasErrors() {
            return errorMsgs.toString().length() > 0;
        }

        public void clearErrors() {
            errorMsgs = new StringBuilder();
        }

        public String getErrorMsg() {
            return errorMsgs.toString();
        }

        private void reset() {
            inputStream = null;
            inputStreamReader = null;
            bufferedReader = null;
            isEOF = false;
            lineCounter = 0;
            clearErrors();
        }
    }


}
