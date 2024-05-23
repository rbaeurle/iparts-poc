/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die DIALOG Baubarkeit (gültige Code zu Baureihe).
 * Die relevante Datenbanktabelle/Versorgungssatzart ist X4E und Y4E
 */

public class SeriesCodesImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    //Felder der DIALOG Baureihen Daten
    private static final String X4E_PREFIX = "X4E";
    public static final String Y4E_PREFIX = "Y4E";
    public static final String IMPORT_TABLENAME_X4E = TABLE_NAME_PREFIX + X4E_PREFIX;
    public static final String IMPORT_TABLENAME_Y4E = TABLE_NAME_PREFIX + Y4E_PREFIX;

    public static final String DEST_TABLENAME = TABLE_DA_SERIES_CODES;

    public static final String BR = "BR";     // PK
    public static final String GRP = "GRP";   // PK
    public static final String POS = "POS";   // PK
    public static final String PV = "PV";     // PK
    public static final String AA = "AA";     // PK
    public static final String SDA = "SDA";   // PK
    public static final String SDB = "SDB";
    public static final String REEL = "REEL";
    public static final String LK = "LK";
    public static final String CGKZ = "CGKZ";
    public static final String ZBED = "ZBED";
    public static final String RFG = "RFG";
    public static final String MG = "MG";
    public static final String VERT = "VERT";
    public static final String FED = "FED";
    public static final String PGKZ = "PGKZ";
    public static final String CBED = "CBED";
    public static final String BKZ = "BKZ";
    public static final String PKZ = "PKZ";
    public static final String EREIA = "EREIA"; // nur Y4E
    public static final String EREIB = "EREIB"; // nur Y4E

    // Für Gültigkeitsprüfungen
    private final Set<String> validCGKZs = new HashSet<>(Arrays.asList("CG", "BG", " ", ""));
    private final Set<String> validBKZs = new HashSet<>(Arrays.asList("J", "N", " ", ""));
    private final Set<String> validPKZs = new HashSet<>(Arrays.asList("J", "N", " ", ""));
    private final String importTableInXML;

    private HashMap<String, String> mapping;
    private String[] primaryKeysForImportData;
    private String prefixForImporterInstance;
    private boolean importToDB = true; // sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;
    private boolean checkEnums = false;  // Überprüfung der Enums CKG,BKZ und PKZ

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public SeriesCodesImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";
        importName = "!!DIALOG Baubarkeit (gültige Code zu Baureihe";
        // Unterscheidung X4E - Y4E
        if (importTableInXML.equals(IMPORT_TABLENAME_X4E)) {
            prefixForImporterInstance = X4E_PREFIX + "_";
            nameForImport = DSC_SERIES_CODES;
            importName += " - " + X4E_PREFIX + ")";
        } else if (importTableInXML.equals(IMPORT_TABLENAME_Y4E)) {
            prefixForImporterInstance = Y4E_PREFIX + "_";
            nameForImport = DSC_SERIES_CODES_EVENT;
            importName += " - " + Y4E_PREFIX + ")";
            mapping.put(FIELD_DSC_EVENT_FROM, prefixForImporterInstance + EREIA);     // nur Y4E
            mapping.put(FIELD_DSC_EVENT_TO, prefixForImporterInstance + EREIB);     // nur Y4E
        }
        // Die Primärschlüsselfelder, wie sie in den zu importierenden Daten (die Namen der Import-Spalten) existieren müssen:
        primaryKeysForImportData = new String[]{ prefixForImporterInstance + BR, prefixForImporterInstance + GRP, prefixForImporterInstance + POS,
                                                 prefixForImporterInstance + PV, prefixForImporterInstance + AA, prefixForImporterInstance + SDA };
        // Restliches Mapping
        mapping.put(FIELD_DSC_SDATB, prefixForImporterInstance + SDB);            //       KEM-Status+Datum- BIS
        mapping.put(FIELD_DSC_REGULATION, prefixForImporterInstance + REEL);      //       Regelelement (BR-AA oder Code), "IPFW", "IPFS", "IP420"
        mapping.put(FIELD_DSC_STEERING, prefixForImporterInstance + LK);          //       Lenkungseinschraenkung  "L", "R", " "
        mapping.put(FIELD_DSC_CGKZ, prefixForImporterInstance + CGKZ);            //       BG-/CG-KZ (Space, 'CG' oder 'BG')
        mapping.put(FIELD_DSC_ZBED, prefixForImporterInstance + ZBED);            //       Zusteuerbedingung
        mapping.put(FIELD_DSC_RFG, prefixForImporterInstance + RFG);              //       Reifegrad der Struktur
        mapping.put(FIELD_DSC_QUANTITY, prefixForImporterInstance + MG);          //       Menge (nur '1' gueltig)
        mapping.put(FIELD_DSC_DISTR, prefixForImporterInstance + VERT);           //       Verteiler (12 x (1 Byte Typ + 10 Byte Verteiler)) bisher nur Werke "A           SB"
        mapping.put(FIELD_DSC_FED, prefixForImporterInstance + FED);              //       Federfuehrende KF (=Konstruktions Freigabe)
        mapping.put(FIELD_DSC_PRODUCT_GRP, prefixForImporterInstance + PGKZ);     //       Produktgruppen-Kennzeichen
        mapping.put(FIELD_DSC_CODES, prefixForImporterInstance + CBED);           //       Codebedingung
        mapping.put(FIELD_DSC_FEASIBILITY_COND, prefixForImporterInstance + BKZ); //       Baubarkeitsbed.-KZ (J/N/Space)
        mapping.put(FIELD_DSC_GLOBAL_CODE_SIGN, prefixForImporterInstance + PKZ); //       Pauschale-Codebed.-KZ (J/N/Space)

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(DEST_TABLENAME,
                                                                                         nameForImport, false, false, false,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
    }

    /**
     * Setzen, welche Datenfelder vorhanden sein müssen und welche Datenfelder Inhalte haben müssen.
     *
     * @param importer
     */
    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysForImportData);
        importer.setMustHaveData(primaryKeysForImportData);
    }

    /**
     * Check-Routine
     *
     * @param importer
     * @return
     */
    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME_X4E)
                   || importer.getTableNames().get(0).equals(IMPORT_TABLENAME_Y4E)
                   || importer.getTableNames().get(0).equals(Y4E_PREFIX);
        }
        return false;
    }

    /**
     * Vor dem Importieren
     */
    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
    }

    /**
     * Es werden alle Stände übernommen
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SeriesCodesImporterHelper importHelper = new SeriesCodesImporterHelper(getProject(), mapping, DEST_TABLENAME);
        if (!importHelper.checkValues(importRec, recordNo, checkEnums)) {
            reduceRecordCount();
            return;
        }

        // Nur die als "versorgungsrelevant" markierten Baureihen importieren.
        String seriesNumber = importHelper.handleValueOfSpecialField(prefixForImporterInstance + BR, importRec);
        if (!importHelper.checkImportRelevanceForSeries(seriesNumber, getInvalidSeriesSet(), this)) {
            return;
        }

        iPartsSeriesCodesDataId dataId = importHelper.getIPartsSeriesCodesDataId(importRec);
        iPartsSeriesCodesData dataSeriesCodes = new iPartsSeriesCodesData(getProject(), dataId);
        if (!dataSeriesCodes.loadFromDB(dataId)) {
            dataSeriesCodes.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else {
            // Datensätze mit SDB = 999999999999999 dürfen Datensätze mit SDB=<gültiges Datum> nicht überschreiben
            String dbSDB = dataSeriesCodes.getFieldValue(FIELD_DSC_SDATB);
            String importSDB = importHelper.handleValueOfSpecialField(prefixForImporterInstance + SDB, importRec);
            if ((!dbSDB.isEmpty()) && (importSDB.isEmpty())) {
                getMessageLog().fireMessage(translateForLog("!!Gültiges SDB %1 darf durch SDB=999999999999999 nicht überschrieben werden!"
                                                            + " DB:Baureihe %2, Gruppe %3, Pos %4, PV %5, AA %6, SDA %7",
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_SDATB),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_SERIES_NO),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_GROUP),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_POS),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_POSV),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_AA),
                                                            dataSeriesCodes.getFieldValue(FIELD_DSC_SDATA)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
                return;
            }
        }
        importHelper.fillOverrideCompleteDataForDIALOGReverse(dataSeriesCodes, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE); // Hat keinen Sprachschlüssel ==> Default [DE]

        if (importToDB) {
            saveToDB(dataSeriesCodes);
        }
    }

    /**
     * Löschen aller Daten
     *
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * Datei einlesen
     *
     * @param importFileType
     * @param importFile
     * @return
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            // für XML-Datei Import
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /*
     * Die Helper Klasse
     */
    private class SeriesCodesImporterHelper extends DIALOGImportHelper {

        public SeriesCodesImporterHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Die Spezialbehandlung für einzelne Felder
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                return "";
            }
            if (sourceField.equals(prefixForImporterInstance + SDA) || sourceField.equals(prefixForImporterInstance + SDB)) {
                return getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(prefixForImporterInstance + VERT)) {
                // Verteiler in eine kommaseparierte Liste verwandeln
                // Maximal: (12 x (1 Byte Typ + 10 Bytes Verteiler))
                // [<X4E_VERT>FFL        FN         FXQ        FZ</X4E_VERT>]
                List<String> list = StrUtils.splitStringIntoSubstrings(value, 11);
                StringBuilder sb = new StringBuilder();
                for (String subString : list) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(subString.trim());
                }
                return sb.toString();

                // Die beiden Felder, die Code enthalten können, Strichpunkt(e) am Ende abschneiden.
            } else if (sourceField.equals(prefixForImporterInstance + ZBED) || sourceField.equals(prefixForImporterInstance + CBED)) {
                return StrUtils.removeAllLastCharacterIfCharacterIs(value.trim(), ";");
            } else {
                return value.trim();
            }
        }

        /**
         * Erzeugt eine für das Objekt ID aus den Importdaten.
         *
         * @param importRec
         * @return
         */
        private iPartsSeriesCodesDataId getIPartsSeriesCodesDataId(Map<String, String> importRec) {
            return new iPartsSeriesCodesDataId(handleValueOfSpecialField(prefixForImporterInstance + BR, importRec),   // PK // Baureihe "C205", "D6519"
                                               handleValueOfSpecialField(prefixForImporterInstance + GRP, importRec),  // PK // Gruppe (3-stellig) "AAM", "CAG"
                                               handleValueOfSpecialField(prefixForImporterInstance + POS, importRec),  // PK // Position "0100"
                                               handleValueOfSpecialField(prefixForImporterInstance + PV, importRec),   // PK // Positionsvariante  "0001"
                                               handleValueOfSpecialField(prefixForImporterInstance + AA, importRec),   // PK // AA der BR (z.B. Hubraumcode) "FW", "FS", "M20"
                                               handleValueOfSpecialField(prefixForImporterInstance + SDA, importRec)); // PK // KEM-Status+Datum- AB
        }


        /**
         * Logische Prüfungen der Importdaten in verschiedenen Konstellationen
         *
         * @param importRec
         * @param recordNo
         * @param checkEnumValues
         * @return true/false für passt/passt nicht.
         */
        protected boolean checkValues(Map<String, String> importRec, int recordNo, boolean checkEnumValues) {
            StringBuilder msg = new StringBuilder();

            // Gruppe, Maximallängenüberprüfung
            String group = handleValueOfSpecialField(prefixForImporterInstance + GRP, importRec);
            if (group.length() > 3) {
                msg.append(translateForLog("!!Ungültige Gruppe: %1!", group));
                msg.append("\n");
            }

            if (checkEnumValues) {
                // Das ominöse BG-/CG-Kennzeichen auf gültige Werte ("CG", "BG", " ", "") überprüfen.
                String cgkz = handleValueOfSpecialField(prefixForImporterInstance + CGKZ, importRec);
                if (!validCGKZs.contains(cgkz)) {
                    msg.append(translateForLog("!!Ungültiges BG-/CG-Kennzeichen: %1!", cgkz));
                    msg.append("\n");
                }

                // Baubarkeitsbedingung auf gültige Werte prüfen
                String bkz = handleValueOfSpecialField(prefixForImporterInstance + BKZ, importRec);
                if (!validBKZs.contains(bkz)) {
                    msg.append(translateForLog("!!Ungültige Baubarkeitsbedingung: %1!", bkz));
                    msg.append("\n");
                }

                // Pauschale Codebedingung auf gültige Werte prüfen
                String pkz = handleValueOfSpecialField(prefixForImporterInstance + PKZ, importRec);
                if (!validPKZs.contains(pkz)) {
                    msg.append(translateForLog("!!Ungültige Pauschale Codebedingung: %1!", pkz));
                    msg.append("\n");
                }
            }
            if (msg.length() > 0) {
                getMessageLog().fireMessage(translateForLog("!!Record %1", String.valueOf(recordNo)) + " " + msg.toString().trim(),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                return false;
            }
            return true;
        }
    }
}
