/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableToPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsFIKZValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Importer für die Zuordnung von Teil zu Farbtabelle (X10E), Farbtabelleninhalt (X9E) sowie ereignisgesteuerteten Farbtabelleninhalt (Y9E)
 */
public class ColorTablePartOrContentImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    public static final String TABLENAME_PREFIX = "T10R";
    public static final String X9E_PREFIX = "X9E";
    public static final String Y9E_PREFIX = "Y9E";
    public static final String X10E_PREFIX = "X10E";
    public static final String IMPORT_TABLENAME_X10E = TABLENAME_PREFIX + X10E_PREFIX; // Strukturdatentabelle für Farbtabelle-Teil
    public static final String IMPORT_TABLENAME_X9E = TABLENAME_PREFIX + X9E_PREFIX;   // Strukturdatentabelle für Farbtabelle/Zuordnung Teil zu Farbe (X9E)
    public static final String IMPORT_TABLENAME_Y9E = TABLENAME_PREFIX + Y9E_PREFIX;   // Strukturdatentabelle für Farbtabelle/Zuordnung Teil zu Farbe (Y9E)

    // In X10E, X9E und Y9E
    public static final String FT = "FT";
    public static final String POS = "POS";
    public static final String SDA = "SDA";
    public static final String SDB = "SDB";
    public static final String FARB = "FARB";
    public static final String PGKZ = "PGKZ";
    public static final String FIKZ = "FIKZ";
    public static final String CBED = "CBED";

    // Nur in X10E
    public static final String X10E_TEIL = "X10E_TEIL";

    // Nur in Y9E
    public static final String Y9E_EREIA = "Y9E_EREIA";
    public static final String Y9E_EREIB = "Y9E_EREIB";


    private HashMap<String, String> mapping;
    private String[] primaryKeysImport;
    private String prefixForImporterInstance;
    private String[] mustExist;
    private String[] mustHave;
    private boolean importToDB = true;
    private String importTableInXML;
    private String importTableInDB;
    private String statusField;
    private String sourceField;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ColorTablePartOrContentImporter(EtkProject project, String xmlTableName) {
        // Erst mit Null initialisieren, weil die Differenzierung der Tabellen erst in initImporter() geschieht.
        super(project, "Invalid Importer");
        if ((xmlTableName == null) || xmlTableName.isEmpty()) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "XML import table must not be null or empty!");
            return;
        }
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
    }


    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";
        // Unterscheidung X10E - X9E - Y9E
        if (importTableInXML.equals(IMPORT_TABLENAME_X10E)) {
            prefixForImporterInstance = X10E_PREFIX + "_";
            nameForImport = DCTP_COLORTABLE_PART;
            importName = "!!DIALOG-Teil zu Farbtabelle (X10E)";
            primaryKeysImport = new String[]{ prefixForImporterInstance + FT, prefixForImporterInstance + POS, prefixForImporterInstance + SDA };
            mustHave = StrUtils.mergeArrays(primaryKeysImport, prefixForImporterInstance + FIKZ);
            // Tabellennamen aus DB und XML + Importname -> X10E
            importTableInDB = TABLE_DA_COLORTABLE_PART;
            sourceField = FIELD_DCTP_SOURCE;
            statusField = FIELD_DCTP_STATUS;
            // Mapping speziell für X10E
            mapping.put(FIELD_DCTP_PART, X10E_TEIL);
            mapping.put(FIELD_DCTP_TABLE_ID, prefixForImporterInstance + FT);
            mapping.put(FIELD_DCTP_SDATA, prefixForImporterInstance + SDA);
            mapping.put(FIELD_DCTP_SDATB, prefixForImporterInstance + SDB);
        } else if (importTableInXML.equals(IMPORT_TABLENAME_X9E)) {
            initColortableContentData(X9E_PREFIX);
            importName = "!!DIALOG-Farbtabelleninhalt (X9E)";
            nameForImport = DCTC_COLORTABLE_CONTENT;
        } else if (importTableInXML.equals(IMPORT_TABLENAME_Y9E)) {
            initColortableContentData(Y9E_PREFIX);
            importName = "!!DIALOG-Farbtabelleninhalt (Y9E)";
            nameForImport = DCTC_COLORTABLE_CONTENT_EVENT;
            mapping.put(FIELD_DCTC_EVENT_FROM, Y9E_EREIA);
            mapping.put(FIELD_DCTC_EVENT_TO, Y9E_EREIB);
        }
        // Bei allen gleich
        if (primaryKeysImport != null) {
            mustExist = StrUtils.mergeArrays(primaryKeysImport, prefixForImporterInstance + FIKZ);
        } else {
            mustExist = new String[]{ prefixForImporterInstance + FIKZ };
        }

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(importTableInDB, nameForImport,
                                                                                         false, false, true,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
    }

    /**
     * Initialisiert gemeinsame Strukturen für Farbtabelleninhalt-Importe (X9E und Y9E)
     *
     * @param prefix
     */
    private void initColortableContentData(String prefix) {
        prefixForImporterInstance = prefix + "_";
        primaryKeysImport = new String[]{ prefixForImporterInstance + FT, prefixForImporterInstance + POS, prefixForImporterInstance + SDA };
        mustHave = StrUtils.mergeArrays(primaryKeysImport, prefixForImporterInstance + FIKZ, prefixForImporterInstance + SDB);
        importTableInDB = TABLE_DA_COLORTABLE_CONTENT;
        sourceField = FIELD_DCTC_SOURCE;
        statusField = FIELD_DCTC_STATUS;
        mapping.put(FIELD_DCTC_TABLE_ID, prefixForImporterInstance + FT);
        mapping.put(FIELD_DCTC_SDATA, prefixForImporterInstance + SDA);
        mapping.put(FIELD_DCTC_SDATB, prefixForImporterInstance + SDB);
        mapping.put(FIELD_DCTC_POS, prefixForImporterInstance + POS);
        mapping.put(FIELD_DCTC_COLOR_VAR, prefixForImporterInstance + FARB);
        mapping.put(FIELD_DCTC_PGRP, prefixForImporterInstance + PGKZ);
        mapping.put(FIELD_DCTC_CODE, prefixForImporterInstance + CBED);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(mustExist);
        importer.setMustHaveData(mustHave);

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(importTableInXML)
                   || importer.getTableNames().get(0).equals(Y9E_PREFIX)
                   || importer.getTableNames().get(0).equals(X10E_PREFIX);
        }
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Helper für weitere Prozesse
        ColorTablePartOrContentImportHelper helper = new ColorTablePartOrContentImportHelper(getProject(), mapping, importTableInDB);
        String fikz = helper.handleValueOfSpecialField(prefixForImporterInstance + FIKZ, importRec);

        // Laut Confluence werden nur 3 FIKZ Typen verarbeitet "B", "F" und "S"
        iPartsFIKZValues fikzType = iPartsFIKZValues.getTypeFromCode(fikz);
        if ((fikzType != iPartsFIKZValues.MANUAL) && (fikzType != iPartsFIKZValues.COLOR_ISSUES) && (fikzType != iPartsFIKZValues.HOLE_PATTERN)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen (FIKZ für Import nicht " +
                                                        "relevant. FIKZ Bedeutung: %4)", String.valueOf(recordNo),
                                                        "FIKZ", fikzType.getFIKZValue(), fikzType.getDescriptionFromDIALOG()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }

        // Datum ab und FarbtabellenID müssen in die richtige Form gebracht werden
        String sdata = helper.handleValueOfSpecialField(prefixForImporterInstance + SDA, importRec);
        String sdatb = helper.handleValueOfSpecialField((prefixForImporterInstance + SDB), importRec);
        String colorTableId = helper.handleValueOfSpecialField(prefixForImporterInstance + FT, importRec);
        String pos = helper.handleValueOfSpecialField(prefixForImporterInstance + POS, importRec);
        IdWithType id;
        EtkDataObject dataObject = null;
        // DatenObjects und Id abhängig von XML Tabelle
        if (importTableInXML.equals(IMPORT_TABLENAME_X10E)) {
            // Pseudo-Position
            String createdPos = ColorTableFactoryDataImporter.makePseudoPos(helper.handleValueOfSpecialField(X10E_TEIL, importRec),
                                                                            pos);
            // ID und DataObject bauen
            id = new iPartsColorTableToPartId(colorTableId, createdPos, sdata);
            dataObject = new iPartsDataColorTableToPart(getProject(), (iPartsColorTableToPartId)id);
        } else if (isColortableContentData()) {
            // ID und DataObject bauen
            id = new iPartsColorTableContentId(colorTableId, pos, sdata);
            dataObject = new iPartsDataColorTableContent(getProject(), (iPartsColorTableContentId)id);
        }
        // Check, ob die Baureihe import-relevant ist
        if (!helper.checkImportRelevanceForSeriesFromColortable(colorTableId, getInvalidSeriesSet(), this)) {
            return;
        }

        if (dataObject == null) {
            return;
        }

        if (!dataObject.existsInDB()) {
            // Existiert noch nicht -> Anlegen
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataObject.setFieldValue(sourceField, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
            // Ein X9E/X10E Datensatz erhält aktuell immer den Status "freigegeben". Sollte sich das irgendesnn einmal
            // ändern, dann muss geklärt werden, wie sich die Statusermittlung bezüglich der Urladung verhalten soll.
            // Laut DAIMLER-6559 erhalten Urldaungsinformationen nämlich immer den Status "freigegeben".
            dataObject.setFieldValue(statusField, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            helper.fillOverrideCompleteDataForDIALOGReverse(dataObject, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        } else {
            if (iPartsImportDataOrigin.getTypeFromCode(dataObject.getFieldValue(sourceField)) == iPartsImportDataOrigin.DIALOG) {
                if (isFinalStateDateTime(sdatb)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, da der Datensatz mit Quelle DIALOG " +
                                                                "bereits existiert und SDB des Importdatensatzes \"%2\" ist",
                                                                String.valueOf(recordNo), sdatb),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    reduceRecordCount();
                    return;
                }
            }
            // Check, ob es sich um einem MAD Datensatz handelt. Falls ja, leere den Datensatz und befülle ihn mit DIALOG Daten.
            helper.deleteContentIfMADSource(dataObject, sourceField, true);
            helper.fillOverrideCompleteDataForDIALOGReverse(dataObject, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);

            // Status setzen falls leer
            if (dataObject.getFieldValue(statusField).isEmpty()) {
                dataObject.setFieldValue(statusField, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            }
        }
        // Setze den ursprünglichen POS Wert bei X10E (bei neuen Datensätzen, bei MAD und bei bestehenden DIALOG Datensätzen)
        if (importTableInXML.equals(IMPORT_TABLENAME_X10E)) {
            dataObject.setFieldValue(FIELD_DCTP_POS_SOURCE, helper.handleValueOfSpecialField(prefixForImporterInstance + POS, importRec), DBActionOrigin.FROM_EDIT);
        } else if (isColortableContentData()) {
            // Code aus DA_ACC_CODES und AS_CODE werden aus der Coderegel entfernt und in DCTC_CODE_AS gespeichert
            Set<String> removedCodes = new HashSet<>();
            List<String> logMessages = new ArrayList<>();
            String codeString = helper.handleValueOfSpecialField(prefixForImporterInstance + CBED, importRec);
            String reducedCodeString = DaimlerCodes.reduceCodeString(getProject(), codeString, removedCodes, logMessages);
            if (!codeString.equals(reducedCodeString) && dataObject.getFieldValue(FIELD_DCTC_CODE_AS).isEmpty()) {
                dataObject.setFieldValue(FIELD_DCTC_CODE_AS, reducedCodeString, DBActionOrigin.FROM_EDIT);
            }
            for (String logMessage : logMessages) {
                getMessageLog().fireMessage(logMessage, MessageLogType.tmlWarning);
            }
        }
        if (importToDB) {
            saveToDB(dataObject);
        }
    }

    private boolean isColortableContentData() {
        return importTableInXML.equals(IMPORT_TABLENAME_X9E) || importTableInXML.equals(IMPORT_TABLENAME_Y9E);
    }


    @Override
    public void postImportTask() {
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(importTableInDB)) {
            getProject().getDB().delete(importTableInDB, new String[]{ sourceField }, new String[]{ iPartsImportDataOrigin.DIALOG.getOrigin() });
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(importTableInDB)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class ColorTablePartOrContentImportHelper extends DIALOGImportHelper {

        public ColorTablePartOrContentImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(X10E_TEIL) || sourceField.equals(prefixForImporterInstance + FT)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            if (sourceField.equals(prefixForImporterInstance + SDA) || sourceField.equals(prefixForImporterInstance + SDB)) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(prefixForImporterInstance + FIKZ)) {
                if (value == null) {
                    value = "";
                }
            }
            return value;
        }
    }
}
