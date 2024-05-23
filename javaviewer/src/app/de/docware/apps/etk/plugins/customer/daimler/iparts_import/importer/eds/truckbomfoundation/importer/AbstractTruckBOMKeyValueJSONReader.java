/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.file.DWFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstrakter KeyValue-Reader für die TruckBOM.foundation Sub-Importer
 */
public abstract class AbstractTruckBOMKeyValueJSONReader extends AbstractKeyValueRecordReader {

    private final int recordCount;
    private int importRecCount;
    private List<RecordData> recordsFromOneDataObject; // Ein Datensatz kann mehrere Versionen enthalten

    public AbstractTruckBOMKeyValueJSONReader(DWFile savedJSONFile, int recordCount, String tableName) {
        super(savedJSONFile, tableName);
        this.recordCount = recordCount;
        this.recordsFromOneDataObject = new ArrayList<>(); // Aus einem Datensatz können mehrere Records entstehen. Diese werden hier gehalten
    }

    protected int getImportRecCount() {
        return importRecCount;
    }

    protected void setImportRecCount(int importRecCount) {
        this.importRecCount = importRecCount;
    }

    protected void incImportRecCount() {
        setImportRecCount(getImportRecCount() + 1);
    }

    @Override
    public Map<String, String> getNextRecord() {
        // Solange die JSON Daten durchgehen, bis mind ein RecordData erzeugt werden konnte
        while (((recordsFromOneDataObject == null) || recordsFromOneDataObject.isEmpty())) {
            // Sind wir beim Holen weiter als es JSON Objekte gibt, dann ist der Import zu Ende
            if ((getImportRecCount() >= getRecordCount())) {
                return null;
            }
            // Die nächsten Datensätze bestimmen (Versionen) und den Zähler um eins erhöhen
            // Pro JSON Einzelobjekt, kann es mehrere Versionen geben. Wir zählen nur die Einzelobjekte und nicht die
            // Versionen eines Einzelobjekts
            recordsFromOneDataObject = getNextRecordData();
            incImportRecCount();
        }
        // Aus einem RecordData wird nun versucht ein importRec zu erzeugen, d.h. wir gehen alle RecordData durch
        // und schauen, ob ein gültiger importRec vorhanden ist. Das machen wir so lange bis alle RecordData für das
        // Einzelobjekt geprüft wurden. Hier wird immer nur ein importRec erzeugt und zurückgegeben. Existieren mehrere,
        // dann wird das nächste beim nächsten getNextRecord() aufruf erzeugt. Der obere Loop wird nicht durchlaufen, da
        // recordsFromOneDataObject ja nicht leer ist
        Map<String, String> importRec = null;
        while ((importRec == null) || importRec.isEmpty()) {
            // Konnte kein importRec erzeugt werden, getNextRecord() aufrufen, da es noch weitere Einzelobjekte geben kann
            if ((recordsFromOneDataObject == null) || recordsFromOneDataObject.isEmpty()) {
                return getNextRecord();
            }
            // Durch den oberen Loop müssen hier Objekte drin sein
            RecordData record = recordsFromOneDataObject.remove(0);
            if (record != null) {
                postProcessRecordData(record);
                importRec = record.getRecord();
            }
        }
        return importRec;
    }

    protected void addTextObjectIfNotNull(String xmlElement, EtkMultiSprache multi, Map<String, EtkMultiSprache> textsForRecord) {
        if (multi != null) {
            textsForRecord.put(xmlElement, multi);
        }
    }

    protected abstract List<RecordData> getNextRecordData();

    protected abstract void postProcessRecordData(RecordData record);


    @Override
    public Map<String, String> getNextRecord(String tableName) {
        return getNextRecord();
    }

    @Override
    public boolean saveFile(DWFile dir, String prefix) {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public int getRecordCount() {
        return recordCount;
    }

    @Override
    public boolean open() throws IOException, SAXException {
        setImportRecCount(0);
        return true;
    }

    @Override
    public String getCurrentTableName() {
        return getOriginalTableName();
    }

    protected abstract String getOriginalTableName();
}
