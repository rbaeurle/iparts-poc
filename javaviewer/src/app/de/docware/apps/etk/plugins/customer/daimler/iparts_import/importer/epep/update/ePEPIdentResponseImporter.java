/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.update;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenFieldDescription;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordType;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordTypeIdentifier;
import de.docware.apps.etk.base.importer.base.model.fixedlength.KeyValueRecordFixedLengthFixGZFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKemResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsKemResponseId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.helper.ePEPImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.sql.TableAndFieldName;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ePEP: Import von Ident-Rückmeldungen, (elektronischer Produktions-Einsatz-Prozess)
 * <p>
 * Es gibt keine Urladung, nur Deltaversorgung über RFTS/x.
 * Die Importdateien kommen gezippt.
 * Die entpackten Dateien haben keine Endung und folgenden Namensaufbau: ePEP_GSP_KEM_FIN_ANTWORT_<YYYYMMDDHHMMSS>
 * --- (Beispiel: ePEP_GSP_KEM_FIN_ANTWORT_20151009233109)
 * Die ePEP-Importdateien sind im fixed Length-Format.
 */
public class ePEPIdentResponseImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String SATZART = "SATZART_EPEP";

    final static int EPEP_SATZART_LENGTH = 44;

    final static public String EPEP_FACTORY_NO = "EPEP_FACTORY_NO";     // Werk, Werksnummer
    final static public String EPEP_KEM = "EPEP_KEM";                   // KEM-Nummer
    final static public String EPEP_FIN = "EPEP_FIN";                   // FIN-Nr
    final static public String EPEP_FIN_DATE = "EPEP_FIN_DATE";         // FIN-Meldedatum
    final static public String EPEP_KEM_UNKNOWN = "EPEP_KEM_UNKNOWN";   // GSP-(=Global Service & Parts)-KEM-Unbekannt

    private String[] primaryKeysEPEPImportData;

    private boolean importToDB = true;
    private boolean doBufferedSave = true;

    public ePEPIdentResponseImporter(EtkProject project) {
        super(project, "!!ePEP Ident-Rückmeldungen (ePEP_GSP_KEM_FIN_ANTWORT)",
              new FilesImporterFileListType("Ident-Rückmeldungen", "!!Ident-Rückmeldungen",
                                            false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_ALL_FILES }));

        primaryKeysEPEPImportData = new String[]{ TableAndFieldName.make(SATZART, EPEP_FACTORY_NO),
                                                  TableAndFieldName.make(SATZART, EPEP_KEM),
                                                  TableAndFieldName.make(SATZART, EPEP_FIN) };

    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysEPEPImportData);
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysEPEPImportData,
                                                      new String[]{ TableAndFieldName.make(SATZART, EPEP_FIN_DATE),
                                                                    TableAndFieldName.make(SATZART, EPEP_KEM_UNKNOWN) }));
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        ePEPImportHelper importHelper = new ePEPImportHelper(getProject(), this, null, "");

        String factory = importRec.get(TableAndFieldName.make(SATZART, EPEP_FACTORY_NO));
        String kem = importHelper.convertKemFromMemoryFormatToInputFormat(importRec.get(TableAndFieldName.make(SATZART, EPEP_KEM)));
        String fin = importRec.get(TableAndFieldName.make(SATZART, EPEP_FIN));
        String finDate = importHelper.handleDateValue(importRec.get(TableAndFieldName.make(SATZART, EPEP_FIN_DATE)));

        if (!StrUtils.isValid(factory, kem, fin, finDate)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Datensatz enthält ungültige Daten.",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
        }
        boolean kemUnknown = importHelper.handleKemUnknownValue(importRec.get(TableAndFieldName.make(SATZART, EPEP_KEM_UNKNOWN)));

        // Jetzt das Objekt zum Speichern der Daten anlegen und wenn vorhanden, aus der Datenbank lesen.
        iPartsKemResponseId kemResponseId = new iPartsKemResponseId(factory, kem, fin);
        iPartsDataKemResponse kemResponse = new iPartsDataKemResponse(getProject(), kemResponseId);
        boolean existsInDB = kemResponse.existsInDB();
        // Bei Bedarf mit leeren Werten initialisieren.
        if (!existsInDB) {
            kemResponse.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }

        // Die Felder setzen, die nicht über den Schlüssel gesetzt werden.
        kemResponse.setFieldValue(FIELD_KRD_FIN_DATE, finDate, DBActionOrigin.FROM_EDIT);
        kemResponse.setFieldValueAsBoolean(FIELD_KRD_KEM_UNKNOWN, kemUnknown, DBActionOrigin.FROM_EDIT);

        if (importToDB) {
            saveToDB(kemResponse);
        }
    }

    @Override
    public boolean finishImport() {
        return super.finishImport(false); // Keine Caches löschen bei diesem Importer
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * Der Importdateiname hat keine Endung: "ePEP_GSP_KEM_FIN_ANTWORT_2019090623300900"
     * Und am enthaltenen Zeitstempel hängen manchmal zwei zu ignorierende Ziffern (hier "00").
     * ==> Es gibt den Zeitstempel im Dateinamen entweder mit 14 oder mit 16 Zeichen.
     * Bei dem Zeitstempel mit 16 Zeichen sind die letzten beiden Stellen zuignorieren.
     *
     * @param importFileType
     * @param importFile
     * @return
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        Date fileDate = ePEPImportHelper.extractDateFromFilename(importFile.getPath(), this);
        if (fileDate != null) {
            setDatasetDate(fileDate);
            return importMasterData(prepareImporterFixedLength(importFile));
        }
        return false;
    }

    /**
     * Hier wird der fixed-length-Aufbau der Zeilen festgelegt.
     *
     * @param textFile
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterFixedLength(DWFile textFile) {
/*
        Jede Importdatei enthält n Datensätze im fixedLength Format
        Das Zeilenende der Importdateien ist nur ein Zeichen im Unix-Format 0x0A (=LF)

        -- Beispieldatensätze:
        154211 BAT1400709           S03325620151005N
        060 11 ZAN150970017         099265720151006N
        154211 SCX1800132           A05026320190903N
        154 11 WAR140287501         B02099220151008N
        154211 XAN180490003         A05026320190903N
            |                      |      |       |
        12345678901234567890123456789012345678901234
                 1         2         3         4

        01-04, Werk-Nr,            4 Stellen ==> [1542]
        05-28, KEM-Nr,            24 Stellen ==> [11 XAN180490003]
        29-35, FIN-NR,             7 Stellen ==> [A050263]
        36-43, FIN-Melde-Datum,    8 Stellen ==> [20190903]
        44-44, GSP-KEM-Unbekannt,  1 Stellen ==> [N]
        ----------------
        --- ACHTUNG! ---
        ----------------
        An einer Importdatei hing hinter dem GSP-KEM-Unbekannt noch ein " X"
        ==> ALSO DOCH(!) auf multi-fixed-length reagieren und den Rest einfach abschneiden.
*/
        FixedLenRecordType[] recordTypes = new FixedLenRecordType[]{
                new FixedLenRecordType(SATZART,
                                       new FixedLenRecordTypeIdentifier[]{}, // kein Identifier
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(1, 4, EPEP_FACTORY_NO),
                                               new FixedLenFieldDescription(5, 28, EPEP_KEM),
                                               new FixedLenFieldDescription(29, 35, EPEP_FIN),
                                               new FixedLenFieldDescription(36, 43, EPEP_FIN_DATE),
                                               new FixedLenFieldDescription(44, 44, EPEP_KEM_UNKNOWN)
                                       }
                )
        };
        // Dieser Reader kann auch unge-zip-pte Dateien erkennen und verarbeiten UND Zeilen länger als "recordPayloadLength" abschneiden.
        return new KeyValueRecordFixedLengthFixGZFileReader(textFile, "", recordTypes, 44, DWFileCoding.UTF8);
    }
}
