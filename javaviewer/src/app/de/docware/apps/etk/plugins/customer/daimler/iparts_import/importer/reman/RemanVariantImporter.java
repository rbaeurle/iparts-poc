/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.reman;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.csv.CsvReader;
import de.docware.util.sql.SQLParameterList;
import de.docware.util.sql.SQLStatement;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Tables;
import de.docware.util.sql.terms.UpdateFields;

import java.sql.SQLException;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Importer für Reman (Austauschmotor) Varianten zur ZB Sachnummer.
 */
public class RemanVariantImporter extends AbstractDataImporter implements iPartsConst {

    private static final String TYPE = "RemanVariantImportType";
    private static final String TABLE_NAME = TABLE_MAT;
    private static final char CSV_IMPORT_SEPARATOR = '\t';

    // Feldnamen in CSV-Header
    private static final String PN_ORIGINAL = "partNumber";
    private static final String PN_BASIC = "partNumber_Basic";
    private static final String PN_SHORTBLOCK = "partNumber_Shortblock";
    private static final String PN_LONGBLOCK = "partNumber_Longblock";
    private static final String PN_LONGBLOCKPLUS = "partNumber_LongblockPlus";

    private boolean isBufferedSave = true;
    private boolean importToDB = true;
    private Map<PartId, EtkDataPart> dataToStoreInDB;
    private Set<String> processedPartNumbers;
    private iPartsNumberHelper numberHelper;

    private String[] headerNames = new String[]{
            PN_ORIGINAL,
            PN_BASIC,
            PN_SHORTBLOCK,
            PN_LONGBLOCK,
            PN_LONGBLOCKPLUS };

    private HashMap<String, String> mappingData;
    private String[] primaryKeysImport = new String[]{ PN_ORIGINAL };

    public RemanVariantImporter(EtkProject project) {

        super(project, REMAN_VARIANTS_NAME, true,
              new FilesImporterFileListType(TYPE, REMAN_VARIANTS_NAME,
                                            false, true, true,
                                            new String[]{ MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_CSV
                                            }));
        numberHelper = new iPartsNumberHelper();
        mappingData = new HashMap<>();
        mappingData.put(FIELD_M_PARTNO_BASIC, PN_BASIC);
        mappingData.put(FIELD_M_PARTNO_SHORTBLOCK, PN_SHORTBLOCK);
        mappingData.put(FIELD_M_PARTNO_LONGBLOCK, PN_LONGBLOCK);
        mappingData.put(FIELD_M_PARTNO_LONGBLOCK_PLUS, PN_LONGBLOCKPLUS);
    }

    /**
     * Die Importdatei enthält immer alle Daten, ein Vollimport also.
     * ==> Die Inhalte aller einzulesenden Spalten/Feld können einfach leer überschrieben werden.
     * Es geht schneller, wenn man nur die Spalten leert, die Daten enthalten.
     * Sonst hat man einen Full-Table-Scan und das dauert länger.
     */
    private boolean clearDatabaseFields() {
        DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.update(new Tables(TABLE_NAME));
        query.set(new UpdateFields(FIELD_M_PARTNO_BASIC, FIELD_M_PARTNO_SHORTBLOCK, FIELD_M_PARTNO_LONGBLOCK, FIELD_M_PARTNO_LONGBLOCK_PLUS));
        query.where(new Condition(FIELD_M_PARTNO_BASIC, Condition.OPERATOR_NOT_EQUALS, "")
                            .or(new Condition(FIELD_M_PARTNO_SHORTBLOCK, Condition.OPERATOR_NOT_EQUALS, "")
                                        .or(new Condition(FIELD_M_PARTNO_LONGBLOCK, Condition.OPERATOR_NOT_EQUALS, "")
                                                    .or(new Condition(FIELD_M_PARTNO_LONGBLOCK_PLUS, Condition.OPERATOR_NOT_EQUALS, ""))))
        );
        SQLParameterList parameterList = new SQLParameterList();
        parameterList.addString("");
        parameterList.addString("");
        parameterList.addString("");
        parameterList.addString("");
        SQLStatement statement = null;
        try {
            statement = getProject().getDB().getDBForDomain(MAIN).getNewStatement();
            statement.executeUpdate(query, parameterList);
            return true;
        } catch (SQLException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
        } finally {
            if (statement != null) {
                statement.release();
            }
        }
        return false;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return clearDatabaseFields();
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, TABLE_NAME, CSV_IMPORT_SEPARATOR, true, headerNames, CsvReader.DEFAULT_QUOTE));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_NAME, CSV_IMPORT_SEPARATOR, true, headerNames));
        }
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(primaryKeysImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            if (!importer.getTableNames().get(0).equals(TABLE_NAME)) {
                getMessageLog().fireMessage(translateForLog("!!Falscher Importtabellenname %2 statt %3",
                                                            importer.getTableNames().get(0),
                                                            TABLE_NAME),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
        dataToStoreInDB = new HashMap<>();
        processedPartNumbers = new HashSet<>();
        setClearCachesAfterImport(importToDB);
        setBufferedSave(isBufferedSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        RemanImportHelper helper = new RemanImportHelper(getProject(), mappingData, TABLE_NAME);

        // Die Original-Teilenummer muss vorhanden sein, sonst kann man die Daten keiner Materialnummer zuordnen.
        // Auch hier ggf. ES1 und ES2 abschneiden.
        String pnOriginal = numberHelper.getPureASachNo(getProject(), helper.handleValueOfSpecialField(PN_ORIGINAL, importRec));

        if (StrUtils.isEmpty(pnOriginal)) {
            getMessageLog().fireMessage(translateForLog("!!Fehlender Schlüssel in der ersten Spalte in Zeile: %1, Zeile wird übersprungen.",
                                                        Integer.toString(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Es sollten eigentlich keine doppelten Materialnummern vorkommen, wenn doch, dann mit Meldung überspringen.
        if (processedPartNumbers.contains(pnOriginal)) {
            getMessageLog().fireMessage(translateForLog("!!Doppelte Materialnummer %1 in Zeile: %2 wird übersprungen.",
                                                        pnOriginal, Integer.toString(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        } else {
            processedPartNumbers.add(pnOriginal);
        }

        // Check, ob die original Materialnummer bekannt ist.
        iPartsPartId partId = new iPartsPartId(pnOriginal, "");
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);
        if (!part.existsInDB()) {
            getMessageLog().fireMessage(translateForLog("!!Die Materialnummer %1 existiert nicht, Zeile: %2 wird übersprungen.",
                                                        pnOriginal, Integer.toString(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        helper.fillOverrideCompleteDataReverse(part, importRec, null);
        buildWarning(importRec, recordNo, part, helper);
        if (part.isModified()) {
            dataToStoreInDB.put(partId, part);
        } else {
            reduceRecordCount();
        }
    }

    /**
     * Anzeige, welche Varianten pro Zeile nicht existieren
     *
     * @param importRec
     * @param recordNo
     * @param part
     * @param helper
     */
    private void buildWarning(Map<String, String> importRec, int recordNo, EtkDataPart part, RemanImportHelper helper) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> mappingForField : mappingData.entrySet()) {
            String dbDestFieldName = mappingForField.getKey();
            String importFieldName = mappingForField.getValue();
            String value = importRec.get(importFieldName);
            if (StrUtils.isEmpty(value)) {
                continue;
            }
            if (StrUtils.isEmpty(part.getFieldValue(dbDestFieldName))) {
                if (str.toString().length() > 0) {
                    str.append(", ");
                }
                str.append(translateForLog("!!\"%1\" (%2) aus Spalte \"%3\"",
                                           value, helper.formatRemanASachNo(getProject(), value), importFieldName));
            }
        }
        if (str.toString().isEmpty()) {
            if (part.isModified()) {
                return;
            }
            str.append(translateForLog("!!Keine Änderungen für die Materialnummer %1, Zeile: %2 wird übersprungen.",
                                       part.getAsId().getMatNr(), Integer.toString(recordNo)));
        } else {
            str.insert(0, translateForLog("Zeile %1 - Fehlende Teilestämme in der DB:", Integer.toString(recordNo), part.getAsId().getMatNr()) + " ");
        }
        getMessageLog().fireMessage(str.toString(),
                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

    /**
     * Hier werden die gesammelten Daten in die Datenbank gespeichert.
     */
    @Override
    protected void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();

        if (importToDB && !dataToStoreInDB.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Speichere %1 Datensätze.", Integer.toString(dataToStoreInDB.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            for (EtkDataPart part : dataToStoreInDB.values()) {
                saveToDB(part);
            }
        }
        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(
                new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MATERIAL,
                                                   iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                   (Collection)null, true));

        ApplicationEvents.fireEventInAllProjectsAndAllClusters(
                new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                   iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                   (Collection)null, true));
    }

    protected class RemanImportHelper extends iPartsMainImportHelper {

        public RemanImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Eine direkte Datenbankabfrage, ob die Teilenummer in der Datenbank steht.
         * Also: ob es sich um eine bekannte (gültige) Teilenummer handelt.
         *
         * @param partNo
         * @return
         */
        protected String validatePartNoExistsInDB(String partNo) {
            // Prüfung, ob das Material existiert
            if (StrUtils.isValid(partNo)) {
                if (getProject().getDB().getRecordExists(EtkDbConst.TABLE_MAT,
                                                         new String[]{ FIELD_M_MATNR },
                                                         new String[]{ partNo })) {
                    return partNo;
                }
            }
            return "";
        }

        protected String extractCorrespondingSubstring(String sourceField, String destField, String sourceValue) {
            // wird hier verwendet um zu überprüfen, ob die MatNo existiert
            return validatePartNoExistsInDB(sourceValue);
        }

        public String formatRemanASachNo(EtkProject project, String aSachNo) {
            if (numberHelper.isValidASachNo(project, aSachNo)) {
                aSachNo = numberHelper.unformatASachNoForDB(project, aSachNo);
                int len = aSachNo.length() - 1;
                String es1 = "";
                String es2 = "";
                switch (len) {
                    case 10: // normale ASachNo (A 123 456 78 90)
                        break;
                    case 12: // ASachNo + ES1
                        es1 = StrUtils.copySubString(aSachNo, 11, 2);
                        break;
                    case 14: // ASachNo + ES2
                        es2 = StrUtils.copySubString(aSachNo, 11, 4);
                        break;
                    case 16: // ASachNo + ES1 + ES2
                        es1 = StrUtils.copySubString(aSachNo, 11, 2);
                        es2 = StrUtils.copySubString(aSachNo, 13, 4);
                        break;
                }
                return numberHelper.getPartNoWithES1AndESKeys(StrUtils.copySubString(aSachNo, 0, 11), es1, es2);
            }
            return "";
        }

        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (!StrUtils.isValid(sourceField, value)) {
                return "";
            } else if (sourceField.equals(PN_BASIC)
                       || sourceField.equals(PN_SHORTBLOCK)
                       || sourceField.equals(PN_LONGBLOCK)
                       || sourceField.equals(PN_LONGBLOCKPLUS)) {
                return formatRemanASachNo(getProject(), value);
            } else {
                return value.trim();
            }
        }
    }
}
