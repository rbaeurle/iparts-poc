/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.ArrayFileImporter;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractFilesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importworker für die
 */
@Deprecated
public class ConstructionDataEdsImporter extends AbstractFilesImporter implements iPartsConst, EtkDbConst {

    // Felder der EDS Baumusterinhalt
    private static final String BM = "BM";
    private static final String GRUPPE = "Gruppe";
    private static final String UMFANG = "Umfang";
    private static final String POS = "POS";
    private static final String UNTERE_SNR = "untere SNR";
    private static final String LKG = "LKG";
    private static final String AA = "AA";
    private static final String REVFROM = "revFrom";
    private static final String SAA_MENGE = "MENGE";
    private static final String BENENNUNG_UNTERESNR = "Benennung untere SNR";
    private static final String CODEBEDINGUNG_BM_BEZOGEN = "Codebedingung, BM-Bezogen";
    private static final String ZUSTEUERUNGSBEDINGUNG = "Zusteuerungsbedingung";
    private static final String SAA_ZGS = "ZGS";

    // Felder der EDS Stammdaten
    private static final String EDS_E_USNR = "E_USNR";
    private static final String EDS_BEN = "BEN";
    private static final String EDS_ZGS = "ZGS";
    private static final String EDS_HINWEISZ = "HINWEISZ";
    private static final String EDS_ZDAT = "ZDAT";
    private static final String EDS_GEWOGEN = "GEWOGEN";
    private static final String EDS_BERECHNET = "BERECHNET";
    private static final String EDS_PROGNOSE = "PROGNOSE";
    private static final String EDS_USNR_AS = "USNR_AS";
    private static final String EDS_ME = "ME";
    private static final String EDS_ETDOK = "ETDOK";
    private static final String EDS_ET_KZ_GESAMT = "ET_KZ_GESAMT";
    private static final String EDS_BEM1 = "BEM1";
    private static final String EDS_BEM2 = "BEM2";
    private static final String EDS_WERKSTOFF = "WERKSTOFF";

    private Map<String, String> edsDescriptionMapping;
    private Map<String, String> edsSAAMapping;
    private Map<String, String> partMapping;


    /**
     * Constructor nur für den Import des Baumusterinhalts
     *
     * @param project
     */
    public ConstructionDataEdsImporter(EtkProject project) {
        super(project, "!!EDS/BCS-Daten",
              new FilesImporterFileListType(TABLE_DA_EDS_MODEL, EDS_MODEL_NAME, false, false, true, MimeTypes.getValidImportExcelAndCsvMimeTypes()),
              new FilesImporterFileListType(TABLE_MAT, DD_EDS_MAT_NAME, false, false, false, MimeTypes.getValidImportExcelAndCsvMimeTypes()));

        // Umsetzen für FakedImport
        edsDescriptionMapping = new HashMap<String, String>();
        edsDescriptionMapping.put(EDS_E_USNR, FIELD_DCP_SNR);
        edsDescriptionMapping.put(EDS_BEN, FIELD_DCP_TEXT);

        // Das Mapping für die EdsSAAfelder in die Edsdatenbankfelder
        edsSAAMapping = new HashMap<String, String>();
        edsSAAMapping.put(BM, FIELD_EDS_MODEL_MODELNO);
        edsSAAMapping.put(GRUPPE, FIELD_EDS_MODEL_GROUP);
        edsSAAMapping.put(UMFANG, FIELD_EDS_MODEL_SCOPE);
        edsSAAMapping.put(POS, FIELD_EDS_MODEL_POS);
        edsSAAMapping.put(LKG, FIELD_EDS_MODEL_STEERING);
        edsSAAMapping.put(AA, FIELD_EDS_MODEL_AA);
        edsSAAMapping.put(REVFROM, FIELD_EDS_MODEL_REVFROM);
        edsSAAMapping.put(UNTERE_SNR, FIELD_EDS_MODEL_MSAAKEY);

        // Das Mapping für die EDS Stammdaten in die Materialbankfelder
        partMapping = new HashMap<String, String>();
        partMapping.put(EDS_E_USNR, FIELD_M_MATNR);
        partMapping.put(EDS_BEN, FIELD_M_CONST_DESC);
        partMapping.put(EDS_ZGS, FIELD_M_IMAGESTATE);
        partMapping.put(EDS_ZDAT, FIELD_M_IMAGEDATE);
        partMapping.put(EDS_GEWOGEN, FIELD_M_WEIGHTREAL);
        partMapping.put(EDS_BERECHNET, FIELD_M_WEIGHTCALC);
        partMapping.put(EDS_PROGNOSE, FIELD_M_WEIGHTPROG);
        partMapping.put(EDS_ME, FIELD_M_QUANTUNIT);
        partMapping.put(EDS_ET_KZ_GESAMT, FIELD_M_ETKZ);
        partMapping.put(EDS_BEM1, FIELD_M_NOTEONE);
        partMapping.put(EDS_BEM2, FIELD_M_NOTETWO);
        partMapping.put(EDS_WERKSTOFF, FIELD_M_MATERIALFINITESTATE);
        partMapping.put(EDS_HINWEISZ, FIELD_M_REFSER);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {

        if (importFileType.getFileListType().equals(TABLE_DA_EDS_MODEL)) {
            getProject().getDB().delete(TABLE_DA_EDS_MODEL);
        }
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_EDS_MODEL)) {
            return importEdsModelFile(importFile);
        }
        if (importFileType.getFileListType().equals(TABLE_MAT)) {
            return importEdsMaterialFile(importFile);
        }
        return false;
    }

    private boolean importEdsModelFile(DWFile importFile) {
        String tableName = TABLE_DA_EDS_MODEL;
        try {
            ArrayFileImporter importer = new ArrayFileImporter();

            getMessageLog().fireMessage(translateForLog("!!Importiere EDS/BCS Baumusterinhalt") + " " + importFile.getName(),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!importer.open(importFile.getPath())) {
                return false;
            }
            // Falls ein Speicher Verzeichnis existiert -> Speicher die Importdatei ab
            if (getDirForSrcFiles() != null) {
                importer.saveSourceFile(importFile, getDirForSrcFiles(), saveFilePrefix);
            }

            // Header erwartet -> also beginne bei 1
            int iRow = 1;

            // In der ersten Zeile des Excels stehen die Spaltenbezeichnungen
            // Lese diese Zeile ein und merke in einer Map die Spaltennummer
            // Später kann über diese Nummer der Record gefüllt werden

            Map<String, Integer> headerNameToIndex = readHeaderLine(importer);

            int maxRow = importer.getRowCount();
            getMessageLog().fireProgress(0, maxRow, "", true, false);

            for (int row = iRow; row < importer.getRowCount(); row++) {
                boolean allEmpty = isEmptyLine(importer, row);

                if (allEmpty) {
                    // Alle Zeilen leer ist das Abbruchkriterium
                    break;
                }

                getMessageLog().fireProgress(row, maxRow, "", true, true);
                // Fülle eine Map mit den Values der Row. Um welches Feld es sich handelt steht in der ersten Zeile
                Map<String, String> data = readCompleteLine(importer, headerNameToIndex, row);
                if (!isModelRowValid(data, row)) {
                    cancelImport(translateForLog("!!Fehler in den Importdaten:") + " " + importFile.getName());
                    return false;
                }

                // jetzt Record bilden und abspeichern
                // Über den primärschlüssel suchen, ob der Datensatz schon da ist
                String[] primaryKeysImportData = new String[]{ BM, GRUPPE, UMFANG, POS, LKG, AA, REVFROM };
                String[] primaryKeysDBFields = new String[primaryKeysImportData.length];
                String[] primaryValues = new String[primaryKeysImportData.length];
                for (int i = 0; i < primaryKeysImportData.length; i++) {
                    String field = primaryKeysImportData[i];
                    String value = data.get(field);
                    if (value == null) {
                        cancelImport(translateForLog("!!Fehler in den Importdaten. Schlüsselfeld \"%1\" ist null oder existiert nicht. Wert: %2", field, value));
                        return false;
                    }
                    primaryValues[i] = value;
                    primaryKeysDBFields[i] = edsSAAMapping.get(primaryKeysImportData[i]);
                }

                boolean recordIsNew = false;
                EtkRecord rec = getProject().getEtkDbs().getRecord(tableName, primaryKeysDBFields, primaryValues);

                if (rec == null) {
                    // Den Datensatz gibt es noch nicht -> Record neu erzeugen
                    rec = new EtkRecord();
                    // Nachdem es diesen noch nicht gibt muss die GUID erzeugt werden
                    //rec.addField(FIELD_DD_GUID, StrUtils.makeGUID());
                    recordIsNew = true;
                }

                // Datensatz mit den Werten füllen
                for (String sourceField : edsSAAMapping.keySet()) {
                    String destField = edsSAAMapping.get(sourceField);
                    String value = data.get(sourceField);
                    if (value != null) {
                        rec.addField(destField, value);
                    }
                }

                List<String> warnings = new ArrayList<String>();
                if (recordIsNew) {
                    getProject().getEtkDbs().insert(tableName, rec, warnings);
                } else {
                    getProject().getEtkDbs().update(tableName, primaryKeysDBFields, primaryValues, rec, warnings);
                }


                // Falls hier noch der Text der SAA eingetragen ist, dann in die deprected SAAD-Tebelle speichern

                String value = data.get(BENENNUNG_UNTERESNR);
                if (value != null) {
                    importEdsSAADescription(data.get(UNTERE_SNR), value);
                }


                for (String warning : warnings) {
                    getMessageLog().fireMessage(warning, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }

            }
            return true;
        } finally {
            getMessageLog().hideProgress();
        }
    }


    private boolean isModelRowValid(Map<String, String> data, int rowNumber) {
        String[] mustExists = new String[]{ BM, GRUPPE, UMFANG, UNTERE_SNR };
        String[] mustHaveData = new String[]{ BM, GRUPPE, UMFANG, UNTERE_SNR };

        boolean result = true;
        if (!checkMustExists(data, rowNumber, mustExists)) {
            result = false;
        }

        if (!checkMustHaveData(data, rowNumber, mustHaveData)) {
            result = false;
        }

        return result;
    }

    /**
     * @param saaNo
     * @param description
     * @return
     * @Deprecated, da die SAAD bald abgeschaft wird. So lange noch nicht komplett umgestellt wurde hier nur ein einfacher Import; EDS_TYPE Feld fehlt hier auch
     */
    private boolean importEdsSAADescription(String saaNo, String description) {
//        String[] primaryKeysDBFields = new String[]{ FIELD_EDS_SAADESCKEY };
//        String[] primaryValues = new String[]{ saaNo };
//
//        boolean recordIsNew = false;
//        DBDataObjectAttributes attributes = getProject().getDbLayer().getAttributes(TABLE_DA_EDS_SAAD, primaryKeysDBFields, primaryValues);
//
//        if (attributes == null) {
//            // Den Datensatz gibt es noch nicht -> Record neu erzeugen
//            attributes = new DBDataObjectAttributes();
//            recordIsNew = true;
//        }
//
//        attributes.addField(FIELD_EDS_SAADESCKEY, saaNo, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
//
//        DBDataObjectAttribute attribute = new DBDataObjectAttribute(FIELD_EDS_SAAD, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
//        attribute.setMultiLanguageText(Language.DE.getCode(), description, null, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
//        attributes.addField(attribute, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
//        getProject().getDbLayer().saveMultiLanguageContentForAttribute(attribute, TABLE_DA_EDS_SAAD, primaryValues[0], null);
//
//        EtkRecord rec = attributes.getAsRecord(false);
//        List<String> warnings = new ArrayList<String>();
//        if (recordIsNew) {
//            getProject().getEtkDbs().insert(TABLE_DA_EDS_SAAD, rec, warnings);
//        } else {
//            getProject().getEtkDbs().update(TABLE_DA_EDS_SAAD, primaryKeysDBFields, primaryValues, rec, warnings);
//        }
//
//        for (String warning : warnings) {
//            getMessageLog().fireMessage(warning, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
//        }
        return true;
    }

    private boolean importEdsMaterialFile(DWFile importFile) {
        try {
            ArrayFileImporter importer = new ArrayFileImporter();

            getMessageLog().fireMessage(translateForLog("!!Importiere EDS/BCS Teile-Daten") + " " + importFile.getName(), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!importer.open(importFile.getPath())) {
                return false;
            }
            // Falls ein Speicher Verzeichnis existiert -> Speicher die Importdatei ab
            if (getDirForSrcFiles() != null) {
                importer.saveSourceFile(importFile, getDirForSrcFiles(), saveFilePrefix);
            }

            // Header erwartet -> also beginne bei 1
            int iRow = 1;

            // In der ersten Zeile des Excels stehen die Spaltenbezeichnungen
            // Lese diese Zeile ein und merke in einer Map die Spaltennummer
            // Später kann über diese Nummer der Record gefüllt werden

            Map<String, Integer> headerNameToIndex = readHeaderLine(importer);

            int maxRow = importer.getRowCount();
            getMessageLog().fireProgress(0, maxRow, "", true, false);

            for (int row = iRow; row < importer.getRowCount(); row++) {
                boolean allEmpty = isEmptyLine(importer, row);

                if (allEmpty) {
                    // Alle Zeilen leer ist das Abbruchkriterium
                    break;
                }

                getMessageLog().fireProgress(row, maxRow, "", true, true);
                // Fülle eine Map mit den Values der Row. Um welches Feld es sich handelt steht in der ersten Zeile
                Map<String, String> data = readCompleteLine(importer, headerNameToIndex, row);
                if (!isMaterialRowValid(data, row)) {
                    cancelImport(translateForLog("!!Fehler in den Importdaten:") + " " + importFile.getName());
                    return false;
                }

                // jetzt Record bilden und abspeichern
                String partNo = data.get(EDS_E_USNR);

                EtkDataPart part = EtkDataObjectFactory.createDataPart();
                part.init(getProject());
                boolean recordIsNew = !part.loadFromDB(new iPartsPartId(partNo, ""));

                if (recordIsNew) {
                    // Das Teil existiert noch nicht in der Datenbank erzeuge es komplett neu
                    part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }

                // Datensatz mit den Werten füllen
                for (String sourceField : partMapping.keySet()) {
                    String destField = partMapping.get(sourceField);
                    String value = data.get(sourceField);
                    if (value != null) {
                        if (getProject().getFieldDescription(TABLE_MAT, destField).isMultiLanguage()) {
                            part.setFieldValue(destField, "DE", value, DBActionOrigin.FROM_EDIT);
                        } else {
                            part.setAttributeValue(destField, value, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
                if (part.attributeExists(FIELD_M_MATNR)) {
                    part.setAttributeValue(FIELD_M_BESTNR, part.getFieldValue(FIELD_M_MATNR), DBActionOrigin.FROM_EDIT);
                }

                // Falls das Teil neu ist, setzte die Konstruktionsbezeichnung als Benennung
                if (part.getFieldValue(FIELD_M_TEXTNR, "DE", true).isEmpty()) {
                    String description = data.get(EDS_BEN);
                    if (description != null) {
                        part.setFieldValue(FIELD_M_TEXTNR, "DE", description, DBActionOrigin.FROM_EDIT);
                    }
                }

                saveToDB(part);
            }
            return true;
        } finally {
            getMessageLog().hideProgress();
        }
    }

    private boolean isMaterialRowValid(Map<String, String> data, int rowNumber) {
        String[] mustExists = new String[]{ EDS_E_USNR };
        String[] mustHaveData = new String[]{ EDS_E_USNR };

        boolean result = true;
        if (!checkMustExists(data, rowNumber, mustExists)) {
            result = false;
        }

        if (!checkMustHaveData(data, rowNumber, mustHaveData)) {
            result = false;
        }

        return result;
    }


    private boolean checkMustExists(Map<String, String> data, int rowNumber, String[] mustExists) {
        boolean result = true;

        for (String field : mustExists) {
            String value = data.get(field);
            if (value == null) {
                getMessageLog().fireMessage(translateForLog("!!Das Feld ist nicht vorhanden:") + " " + field
                                            + " " + translateForLog("!!Zeile:") + " " + Integer.toString(rowNumber),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                result = false;
            }
        }
        return result;
    }

    private boolean checkMustHaveData(Map<String, String> data, int rowNumber, String[] mustHaveData) {
        boolean result = true;
        for (String field : mustHaveData) {
            String value = data.get(field);
            // Feld vorhanden wurde schon geprüft -> nur Inhalte testen
            if ((value != null) && value.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Das Feld ist nicht gefüllt:") + " " + field
                                            + " " + translateForLog("!!Zeile:") + " " + Integer.toString(rowNumber),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                result = false;
            }
        }
        return result;
    }

    private Map<String, Integer> readHeaderLine(ArrayFileImporter importer) {
        Map<String, Integer> headerNameToIndex = new HashMap<String, Integer>();
        for (int col = 0; col < importer.getColCount(); col++) {
            headerNameToIndex.put(importer.getAt(col, 0), col);
        }
        return headerNameToIndex;
    }

    private boolean isEmptyLine(ArrayFileImporter importer, int row) {
        boolean allEmpty = true;
        for (int col = 0; col < importer.getColCount(); col++) {
            if (!importer.getAt(col, row).isEmpty()) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }

    private Map<String, String> readCompleteLine(ArrayFileImporter importer, Map<String, Integer> headerNameToIndex, int row) {
        Map<String, String> data = new HashMap<String, String>();
        for (String key : headerNameToIndex.keySet()) {
            int index = headerNameToIndex.get(key);
            String value = importer.getAt(index, row);
            data.put(key, value);
        }
        return data;
    }


}
