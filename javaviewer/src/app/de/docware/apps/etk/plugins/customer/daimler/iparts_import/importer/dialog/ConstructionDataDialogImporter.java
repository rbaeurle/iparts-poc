/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

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
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importworker für die
 */
@Deprecated
public class ConstructionDataDialogImporter extends AbstractFilesImporter implements iPartsConst, EtkDbConst {

    // Dialognamen der Felder (stehen so im Header)
    private static final String DIALOG_PGKZ = "PGKZ";
    private static final String DIALOG_BR = "BR";
    private static final String DIALOG_RASTER = "RASTER";
    private static final String DIALOG_POSE = "POSE";
    private static final String DIALOG_SESI = "SESI";
    private static final String DIALOG_POSP = "POSP";
    private static final String DIALOG_POSV = "POSV";
    private static final String DIALOG_WW = "WW";
    private static final String DIALOG_ETZ = "ETZ";
    private static final String DIALOG_AA = "AA";
    private static final String DIALOG_SDATA = "SDATA";
    private static final String DIALOG_SDATB = "SDATB";
    private static final String DIALOG_KEMA = "KEMA";
    private static final String DIALOG_KEMB = "KEMB";
    private static final String DIALOG_STEUA = "STEUA";
    private static final String DIALOG_STEUB = "STEUB";
    private static final String DIALOG_FED = "FED";
    private static final String DIALOG_TEIL = "TEIL";
    private static final String DIALOG_L = "L";
    private static final String DIALOG_MGKZ = "MGKZ";
    private static final String DIALOG_MG = "MG";
    private static final String DIALOG_RFMEA = "RFMEA";
    private static final String DIALOG_RFMEN = "RFMEN";
    private static final String DIALOG_BZAE = "BZAE";
    private static final String DIALOG_PTE = "PTE";
    private static final String DIALOG_KGUM = "KGUM";
    private static final String DIALOG_STR = "STR";
    private static final String DIALOG_RFG = "RFG";
    private static final String DIALOG_VERT = "VERT";
    private static final String DIALOG_VARGEN = "VARGEN";
    private static final String DIALOG_VARMET = "VARMET";
    private static final String DIALOG_GESETZ = "GESETZ";
    private static final String DIALOG_PROJ = "PROJ";
    private static final String DIALOG_ETKZ = "ETKZ";
    private static final String DIALOG_CR = "CR";
    private static final String DIALOG_BZAE_NEU = "BZAE_NEU";

    // Hm/M/SM aus Raster aufgedrösel, wird vom Importer gemacht
    private static final String DIALOG_HM = "HM";
    private static final String DIALOG_M = "M";
    private static final String DIALOG_SM = "SM";

    // Feldnamen der Importdatei für die Parts
    private static final String DIALOG_DESC = "DESC";
    private static final String DIALOG_EHM = "EHM";
    private static final String DIALOG_FARBKZ = "FARBKZ";
    private static final String DIALOG_VEDOC = "VEDOC";
    private static final String DIALOG_FGW1 = "FGW1";
    private static final String DIALOG_FGW3 = "FGW3";
    private static final String DIALOG_ZBKZ = "ZBKZ";
    private static final String DIALOG_ZDATUM = "ZDATUM";
    private static final String DIALOG_ZGS = "ZGS";
    private static final String DIALOG_ZSIEHE = "ZSIEHE";
    private static final String DIALOG_EATTR = "EATTR";
    private static final String DIALOG_MATERIALFINITESTATE = "MATERIALFINITESTATE";

    private Map<String, String> dialogMapping;
    private Map<String, String> partMapping;


    public ConstructionDataDialogImporter(EtkProject project) {
        super(project, "!!DIALOG-Daten",
              new FilesImporterFileListType(TABLE_DA_DIALOG, DD_PARTSLIST_NAME, false, false, true, MimeTypes.getValidImportExcelAndCsvMimeTypes()),
              new FilesImporterFileListType(TABLE_MAT, DD_EDS_MAT_NAME, false, false, false, MimeTypes.getValidImportExcelAndCsvMimeTypes()));

        // Das Mapping für die Dialogfelder in die Dialogdatenbankfelder
        dialogMapping = new HashMap<String, String>();
        dialogMapping.put(DIALOG_BR, FIELD_DD_SERIES_NO);
        dialogMapping.put(DIALOG_ETKZ, FIELD_DD_ETKZ);
        dialogMapping.put(DIALOG_HM, FIELD_DD_HM);
        dialogMapping.put(DIALOG_M, FIELD_DD_M);
        dialogMapping.put(DIALOG_SM, FIELD_DD_SM);
        dialogMapping.put(DIALOG_POSE, FIELD_DD_POSE);
        dialogMapping.put(DIALOG_POSV, FIELD_DD_POSV);
        dialogMapping.put(DIALOG_WW, FIELD_DD_WW);
        dialogMapping.put(DIALOG_STR, FIELD_DD_HIERARCHY);
        dialogMapping.put(DIALOG_ETZ, FIELD_DD_ETZ);
        dialogMapping.put(DIALOG_TEIL, FIELD_DD_PARTNO);
        dialogMapping.put(DIALOG_CR, FIELD_DD_CODES);
        dialogMapping.put(DIALOG_L, FIELD_DD_STEERING);
        dialogMapping.put(DIALOG_AA, FIELD_DD_AA);
        dialogMapping.put(DIALOG_MG, FIELD_DD_QUANTITY);
        dialogMapping.put(DIALOG_RFG, FIELD_DD_RFG);
        dialogMapping.put(DIALOG_KEMA, FIELD_DD_KEMA);
        dialogMapping.put(DIALOG_KEMB, FIELD_DD_KEMB);
        dialogMapping.put(DIALOG_SDATA, FIELD_DD_SDATA);
        dialogMapping.put(DIALOG_SDATB, FIELD_DD_SDATB);

        partMapping = new HashMap<String, String>();
        partMapping.put(DIALOG_TEIL, FIELD_M_MATNR);
        partMapping.put(DIALOG_DESC, FIELD_M_CONST_DESC);
        partMapping.put(DIALOG_EHM, FIELD_M_QUANTUNIT);
        partMapping.put(DIALOG_FARBKZ, FIELD_M_VARIANT_SIGN);
        partMapping.put(DIALOG_VEDOC, FIELD_M_VEDOCSIGN);
        partMapping.put(DIALOG_FGW1, FIELD_M_WEIGHTCALC);
        partMapping.put(DIALOG_FGW3, FIELD_M_WEIGHTREAL);
        partMapping.put(DIALOG_ZBKZ, FIELD_M_ASSEMBLYSIGN);
        partMapping.put(DIALOG_ZDATUM, FIELD_M_IMAGEDATE);
        partMapping.put(DIALOG_ZGS, FIELD_M_IMAGESTATE);
        partMapping.put(DIALOG_ZSIEHE, FIELD_M_REFSER);
        partMapping.put(DIALOG_EATTR, FIELD_M_SECURITYSIGN);
        partMapping.put(DIALOG_MATERIALFINITESTATE, FIELD_M_MATERIALFINITESTATE);

    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_DIALOG)) {
            getProject().getDB().delete(TABLE_DA_DIALOG);
        }
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_DIALOG)) {
            return importDialogFile(importFile);
        }
        if (importFileType.getFileListType().equals(TABLE_MAT)) {
            return importDialogMaterialFile(importFile);
        }
        return false;
    }

    private boolean importDialogMaterialFile(DWFile importFile) {
        try {
            ArrayFileImporter importer = new ArrayFileImporter();

            getMessageLog().fireMessage(translateForLog("!!Importiere Dialog-Teile-Daten") + " " + importFile.getName(),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!importer.open(importFile.getPath())) {
                return false;
            }
            // Falls ein Speicher Verzeichnis existiert -> Speicher die Importdatei ab
            if (getDirForSrcFiles() != null) {
                importer.saveSourceFile(importFile, getDirForSrcFiles(), importName);
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

                // Partno muss da sein, sonst hätte die RowValid versagt
                String partNo = data.get(DIALOG_TEIL);

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
                // Falls noch keine Benennung da ist -> setzte die Konstruktionsbezeichnung als Benennung
                if (part.getFieldValue(FIELD_M_TEXTNR, "DE", true).isEmpty()) {
                    String description = data.get(DIALOG_DESC);
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
        String[] mustExists = new String[]{ DIALOG_TEIL };
        String[] mustHaveData = new String[]{ DIALOG_TEIL };

        boolean result = true;
        if (!checkMustExists(data, rowNumber, mustExists)) {
            result = false;
        }

        if (!checkMustHaveData(data, rowNumber, mustHaveData)) {
            result = false;
        }

        return result;
    }

    protected boolean importDialogFile(DWFile importFile) {
        String tableName = TABLE_DA_DIALOG;
        try {
            ArrayFileImporter importer = new ArrayFileImporter();

            getMessageLog().fireMessage(translateForLog("!!Importiere Dialog-Daten") + " " + importFile.getName(),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!importer.open(importFile.getPath())) {
                return false;
            }
            // Falls ein Speicher Verzeichnis existiert -> Speicher die Importdatei ab
            if (getDirForSrcFiles() != null) {
                importer.saveSourceFile(importFile, getDirForSrcFiles(), importName);
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

                getMessageLog().fireProgress(row, maxRow, "", true, true);

                boolean allEmpty = isEmptyLine(importer, row);

                if (allEmpty) {
                    // Alle Zeilen leer ist das Abbruchkriterium
                    break;
                }


                // Fülle eine Map mit den Values der Row. Um welches Feld es sich handelt steht in der ersten Zeile
                Map<String, String> data = readCompleteLine(importer, headerNameToIndex, row);


                if (!isDialogRowValid(data, row)) {
                    cancelImport(translateForLog("!!Fehler in den Importdaten:") + " " + importFile.getName());
                    return false;
                }

                // Jetzt die HM/M/SM extrahieren. Ein Test, ob die 6 Zeichen lang ist ist nicht nötig, das wurde in ValidRow getestet
                data.put(DIALOG_HM, data.get(DIALOG_RASTER).substring(0, 2));
                data.put(DIALOG_M, data.get(DIALOG_RASTER).substring(2, 4));
                data.put(DIALOG_SM, data.get(DIALOG_RASTER).substring(4, 6));

                // Über den kuriosen Dialogprimärschlüssel suchen, ob der Datensatz schon da ist
                String[] primaryKeysImportData = new String[]{ DIALOG_BR, DIALOG_HM, DIALOG_M, DIALOG_SM, DIALOG_POSE, DIALOG_POSV, DIALOG_AA, DIALOG_WW, DIALOG_ETZ, DIALOG_SDATA };
                String[] primaryKeysDBFields = new String[primaryKeysImportData.length];
                String[] primaryValues = new String[primaryKeysImportData.length];
                for (int i = 0; i < primaryKeysImportData.length; i++) {
                    primaryValues[i] = data.get(primaryKeysImportData[i]);
                    primaryKeysDBFields[i] = dialogMapping.get(primaryKeysImportData[i]);
                }

                boolean recordIsNew = false;
                EtkRecord rec = getProject().getEtkDbs().getRecord(tableName, primaryKeysDBFields, primaryValues);

                if (rec == null) {
                    // Den Datensatz gibt es noch nicht -> Record neu erzeugen
                    rec = new EtkRecord();
                    // Nachdem es diesen noch nicht gibt muss die GUID erzeugt werden
                    rec.addField(FIELD_DD_GUID, StrUtils.makeGUID());
                    recordIsNew = true;
                }

                // Datensatz mit den Werten füllen
                for (String sourceField : dialogMapping.keySet()) {
                    String destField = dialogMapping.get(sourceField);
                    String value = data.get(sourceField);
                    if (value != null) {
                        rec.addField(destField, value);
                    }
                }

                List<String> warnings = new ArrayList<String>();
                if (recordIsNew) {
                    getProject().getEtkDbs().insert(tableName, rec, warnings);
                } else {
                    getProject().getEtkDbs().update(tableName, new String[]{ FIELD_DD_GUID }, new String[]{ rec.getField(FIELD_DD_GUID).getAsString() }, rec, warnings);
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

    private boolean isDialogRowValid(Map<String, String> data, int rowNumber) {
        String[] mustExists = new String[]{ DIALOG_BR, DIALOG_RASTER, DIALOG_POSE, DIALOG_POSV, DIALOG_AA, DIALOG_WW, DIALOG_ETZ, DIALOG_SDATA, DIALOG_TEIL };
        String[] mustHaveData = new String[]{ DIALOG_BR, DIALOG_RASTER, DIALOG_POSE, DIALOG_POSV, DIALOG_AA, DIALOG_SDATA, DIALOG_TEIL };

        boolean result = true;
        if (!checkMustExists(data, rowNumber, mustExists)) {
            result = false;
        }

        if (!checkMustHaveData(data, rowNumber, mustHaveData)) {
            result = false;
        }

        String value = data.get(DIALOG_RASTER);
        // Raster muss 6 Stellen lang sein, ob das Feld da ist wurde oben schon geprüft
        if ((value != null) && (value.length() != 6)) {
            getMessageLog().fireMessage(translateForLog("!!Das Feld RASTER ist keine 6 Stellen lang") + " " + translateForLog("!!Zeile:")
                                        + " " + Integer.toString(rowNumber), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            result = false;
        }

        return result;
    }

    private boolean checkMustExists(Map<String, String> data, int rowNumber, String[] mustExists) {
        boolean result = true;

        for (String field : mustExists) {
            String value = data.get(field);
            if (value == null) {
                getMessageLog().fireMessage(translateForLog("!!Das Feld ist nicht vorhanden:") + " " + field + " " + translateForLog("!!Zeile:")
                                            + " " + Integer.toString(rowNumber), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
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
                getMessageLog().fireMessage(translateForLog("!!Das Feld ist nicht gefüllt:") + " " + field + " " + translateForLog("!!Zeile:")
                                            + " " + Integer.toString(rowNumber), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
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
