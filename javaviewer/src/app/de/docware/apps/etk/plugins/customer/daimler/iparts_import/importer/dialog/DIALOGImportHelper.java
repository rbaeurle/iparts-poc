/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDialogChangesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDIALOGSeriesValidityCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsSDBValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Hilfsklasse für das Umsetzen der ImportRecord Data auf die Attribute-Data via Mapping
 * Wird mittlerweile auch für EDS/ELDAS verwendet.  Name also nicht mehr ganz richtig.
 */
public class DIALOGImportHelper extends iPartsMainImportHelper {

    public DIALOGImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }


    /**
     * Weist einem SMART Material mit Farbe die Attribute des farblosen Teils zu.
     *
     * @param project
     * @param smartPartWithColor
     * @param messageLog
     */
    private static void handleSMARTPart(EtkProject project, EtkDataPart smartPartWithColor, EtkMessageLog messageLog, String messageLogLanguage) {
        // Sonderbehandlung für SMART:
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        String smartPartNo = smartPartWithColor.getAsId().getMatNr();
        // DAIMLER-14238, Sonderbehandlung für SMART, bei 18-stelligen Q-Sachnummern ES1, ES2 und die M_BASE_MATNR gezielt leer setzen!
        // "Q + V + 18 Zeichen"-Prüfung
        if (numberHelper.isSMARTPrintedPartNo(smartPartNo)) {
            // Den Teilestamm-Datensatz (ohne Farbanhang) ermitteln und die Attribute übernehmen
            iPartsPartId basePartId = new iPartsPartId(numberHelper.getSMARTBasePartNo(smartPartNo), "");
            EtkDataPart basePart = EtkDataObjectFactory.createDataPart(project, basePartId);
            if (basePart.existsInDB()) {
                PartId smartPartWithColorId = smartPartWithColor.getAsId();
                DBDataObjectAttributes baseAttributesClone = basePart.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
                DBDataObjectAttributes smartPartWithColorAttributes = smartPartWithColor.getAttributes();
                // M_BESTNR, M_MATNR_DTAG und M_MATNR_MBAG dürfen nicht übernommen werden, da die Teilenummer ohne
                // Farbe eine andere ist als die Teilenummer mit Farbe! Bestehende Werte einfach kopieren.
                baseAttributesClone.getField(FIELD_M_BESTNR).setValueAsString(smartPartWithColorAttributes.getFieldValue(FIELD_M_BESTNR), DBActionOrigin.FROM_DB);
                baseAttributesClone.getField(FIELD_M_MATNR_DTAG).setValueAsString(smartPartWithColorAttributes.getFieldValue(FIELD_M_MATNR_DTAG), DBActionOrigin.FROM_DB);
                baseAttributesClone.getField(FIELD_M_MATNR_MBAG).setValueAsString(smartPartWithColorAttributes.getFieldValue(FIELD_M_MATNR_MBAG), DBActionOrigin.FROM_DB);
                smartPartWithColor.assignAttributes(project, baseAttributesClone, false, DBActionOrigin.FROM_EDIT);
                smartPartWithColor.setId(smartPartWithColorId, DBActionOrigin.FROM_EDIT);
                smartPartWithColor.updateOldId();
            } else if (messageLog != null) {
                messageLog.fireMessage(TranslationHandler.translateForLanguage("!!Für die SMART Sachnummer \"%1\" existiert kein Grundstamm \"%2\" in der Datenbank",
                                                                               messageLogLanguage, smartPartWithColor.getAsId().getMatNr(),
                                                                               basePartId.getMatNr()),
                                       MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            // DAIMLER-14238, Sonderbehandlung für SMART, bei 18-stelligen Q-Sachnummern ES1, ES2 und die M_BASE_MATNR gezielt leer setzen!
            smartPartWithColor.setFieldValue(FIELD_M_BASE_MATNR, "", DBActionOrigin.FROM_EDIT);
            smartPartWithColor.setFieldValue(FIELD_M_AS_ES_1, "", DBActionOrigin.FROM_EDIT);
            smartPartWithColor.setFieldValue(FIELD_M_AS_ES_2, "", DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Teilenummern können ES1 und ES2 Schlüssel haben. Falls das der Fall ist müssen die zusätzlichen Felder
     * FIELD_M_AS_ES_1, FIELD_M_AS_ES_2 und FIELD_M_BASE_MATNR mit den Zusatzinformationen befüllt werden. Handelt es
     * sich aber um eine SMART (QV Nummer, dann muss diese anders verarbeitet werden
     *
     * @param part
     */
    public static void handleESKeysInDataPart(EtkProject project, EtkDataPart part, EtkMessageLog messageLog, String messageLogLanguage) {
        String partNo = part.getAsId().getMatNr();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        if (numberHelper.isSMARTPrintedPartNo(partNo)) {
            handleSMARTPart(project, part, messageLog, messageLogLanguage);
        } else if (numberHelper.isPartNoWithESKeys(partNo)) {
            String es1Key = numberHelper.getES1FromDialogInputPartNo(partNo);
            String es2Key = numberHelper.getES2FromDialogInputPartNo(partNo);
            String partNoBase = numberHelper.getBasePartNoFromDialogInputPartNo(partNo);
            part.setFieldValue(FIELD_M_AS_ES_1, es1Key, DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_AS_ES_2, es2Key, DBActionOrigin.FROM_EDIT);
            part.setFieldValue(FIELD_M_BASE_MATNR, partNoBase, DBActionOrigin.FROM_EDIT);
        }
    }

    public static void handleESKeysInDataPart(EtkProject project, EtkDataPart part) {
        handleESKeysInDataPart(project, part, null, "");
    }

    /**
     * Setzt den Status auf "nicht relevant" falls der aktuelle Status "neu" oder "freigegeben" ist
     *
     * @return <code>true</code> falls der Status geändert wurde, sonst <code>false</code>
     */
    public static boolean setNotRelevantStateIfAllowed(EtkDataObject dataObject, String statusField) {
        iPartsDataReleaseState currentReleaseState = iPartsDataReleaseState.getTypeByDBValue(dataObject.getFieldValue(statusField));
        if (currentReleaseState.isReleasedOrNew()) {
            dataObject.setFieldValue(statusField, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(), DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }

    /**
     * Speichert die übergebenen Objekte die von {@link EtkDataObject} erben via übergebenen {@link AbstractDIALOGDataImporter} ab
     *
     * @param importer
     * @param values
     */
    public static void saveCollectedObjects(AbstractDIALOGDataImporter importer, Collection<? extends EtkDataObject> values) {
        // Jetzt alle importierten Datensätze speichern
        importer.getMessageLog().fireMessage(importer.translateForLog("!!Bearbeite %1 Datensätze...", String.valueOf(values.size())),
                                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        int maxProgress = values.size();
        importer.getMessageLog().fireProgress(0, maxProgress, "", true, false);
        int progressCounter = 0;
        for (EtkDataObject dataObject : values) {
            if (importer.cancelImportIfInterrupted()) {
                break;
            }
            importer.saveToDB(dataObject); // Import-Datensatz speichern
            progressCounter++;
            importer.getMessageLog().fireProgress(progressCounter, maxProgress, "", true, true);
        }
        importer.getMessageLog().fireProgress(maxProgress, maxProgress, "", true, false);
    }

    /**
     * Spezielle SDB Prüfung für alle Generic-Importer aus DIALOG (Generic Part und Generic Install Location). Wenn ein
     * SDB zu einem Datensatz existiert, darf der Datensatz nicht verändert werden.
     *
     * @param importData
     * @param existingDataInImportList
     * @param sdbFieldName
     * @param importRec
     * @param recordNo
     * @param importer
     * @param <T>
     * @return
     */
    public <T extends EtkDataObject> Optional<T> doGenericDataSDBCheck(T importData, T existingDataInImportList,
                                                                       String sdbFieldName, Map<String, String> importRec,
                                                                       int recordNo, AbstractDIALOGDataImporter importer) {
        T result = importData;
        if (!result.existsInDB() && (existingDataInImportList == null)) {
            result.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        } else {
            String existingSDATB;
            if (existingDataInImportList == null) {
                // Wenn bereits ein Datensatz mit echtem SDATB-Datum in der DB existiert, dann muss dieser neue Datensatz ignoriert werden
                existingSDATB = result.getFieldValue(sdbFieldName);
            } else {
                // wenn bereits ein Datensatz mit gleichem Primärschlüssel in der Speicher Liste enthalten ist, muss dieser auch berücksichtigt werden
                existingSDATB = existingDataInImportList.getFieldValue(sdbFieldName);
                // Falls der Datensatz noch nicht in der DB vorhanden ist, aber in der Speicherliste dann über die Speicherliste initialisieren
                // Daten werden später überschrieben
                result = existingDataInImportList;
            }
            if (!existingSDATB.isEmpty()) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 übersprungen (bereits vorhandener Datensatz mit echtem Datum Bis)",
                                                                              String.valueOf(recordNo)), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                importer.reduceRecordCount();
                return Optional.empty();
            }
        }
        fillOverrideCompleteDataForDIALOGReverse(result, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        return Optional.of(result);
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (DIALOG spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = Import-Spaltenname, Value = DB-FieldName
     * <br/>Normalerweise sollte statt dieser Methode {@link #fillOverrideCompleteDataForDIALOGReverse(EtkDataObject, Map, iPartsDIALOGLanguageDefs)}
     * verwendet werden.
     *
     * @param dataObject
     * @param importRec
     * @param langDef    DIALOG spezifisch
     */
    public void fillOverrideCompleteDataForDIALOG(EtkDataObject dataObject, Map<String, String> importRec, iPartsDIALOGLanguageDefs langDef) {
        // über alle Mapping Felder
        for (Map.Entry<String, String> mappingForField : mapping.entrySet()) {
            String importFieldName = mappingForField.getKey();
            // Name und value in der DB bestimmen
            String dbDestFieldName = mappingForField.getValue();
            String value = importRec.get(importFieldName);
            importValue(dataObject, importFieldName, dbDestFieldName, value, langDef.getDbValue());
        }
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (DIALOG spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef    DIALOG spezifisch
     */
    public void fillOverrideCompleteDataForDIALOGReverse(EtkDataObject dataObject, Map<String, String> importRec, iPartsDIALOGLanguageDefs langDef) {
        Language language = (langDef != null) ? langDef.getDbValue() : null;
        fillOverrideCompleteDataReverse(dataObject, importRec, language);
    }

    /**
     * in einem DataRecord alle MultiLang Texte zu einer Sprache setzen bzw überschreiben (DIALOG spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef    DIALOG spezifisch
     */
    public void fillOverrideLanguageTextForDIALOGReverse(EtkDataObject dataObject, Map<String, String> importRec, iPartsDIALOGLanguageDefs langDef) {
        // über alle Mapping Felder
        for (Map.Entry<String, String> mappingForField : mapping.entrySet()) {
            String dbDestFieldName = mappingForField.getKey();
            String importFieldName = mappingForField.getValue();
            //nur MultiLanguage-Felder werden behandelt
            if (getProject().getFieldDescription(tableName, dbDestFieldName).isMultiLanguage()) {
                //einen Text zu einer Sprache setzen
                fillOverrideOneLanguageText(dataObject, langDef.getDbValue(), dbDestFieldName, importRec.get(importFieldName));
            }
        }
    }

    /**
     * einen Multilang Text (destField) zu einer Sprache (langDef) setzen bzw überschreiben (DIALOG spezifisch)
     *
     * @param dataObject
     * @param langDef    DIALOG spezifisch
     * @param destField  Feld in der Datenbank
     * @param value
     */
    public void fillOverrideOneLanguageTextForDIALOG(EtkDataObject dataObject, iPartsDIALOGLanguageDefs langDef, String destField, String value) {
        fillOverrideOneLanguageText(dataObject, langDef.getDbValue(), destField, value);
    }

    /**
     * Die typischen SDA/SDB-Felder parsen die es in vielen Importen gibt
     *
     * @param value
     * @return
     */
    protected String getDIALOGDateTimeValue(String value) {
        iPartsDialogDateTimeHandler dtHandler = new iPartsDialogDateTimeHandler(value);
        value = dtHandler.getDBDateTime();
        if (value == null) {
            value = "";
        }
        return value;
    }


    /**
     * Die typischen PEMTA/PEMTB-Felder parsen die es in vielen Importen gibt
     *
     * @param value
     * @return
     */
    protected String getDIALOGDateValueForPEMT(String value) {
        iPartsDialogDateTimeHandler dtHandler = new iPartsDialogDateTimeHandler(value);
        value = dtHandler.getDBDateForPEMT(getProject());
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * Überprüft, ob die in dem übergebenen Import-Record enthaltene Baureihe versorgungsrelevant ist. Falls nicht, werden die entsprechenden Log-Ausgaben
     * in das MessageLog des übergebenen Importers geschrieben (samt reduceRecordCount() Aufruf)
     *
     * @param sourceField
     * @param importRec
     * @param invalidSeriesSet
     * @param importer
     * @return
     */
    public boolean checkImportRelevanceForSeries(String sourceField, Map<String, String> importRec, Set<String> invalidSeriesSet, AbstractDataImporter importer) {
        String seriesNumber = handleValueOfSpecialField(sourceField, importRec);
        if (StrUtils.isEmpty(seriesNumber)) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Datensatz enthält keine Baureihe und wird als \"nicht relevant für Import\" interpretiert!"),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            importer.reduceRecordCount();
            return false;
        }
        return checkImportRelevanceForSeries(seriesNumber, invalidSeriesSet, importer);
    }

    /**
     * Überprüft, ob der übergebene Datensatz DIALOG-seitig bereits freigegeben ist.
     *
     * @param dateField
     * @param importRec
     * @param importer
     * @return
     */
    public boolean checkImportReleaseDateValid(String dateField, Map<String, String> importRec, AbstractDataImporter importer) {
        String releaseDate = handleValueOfSpecialField(dateField, importRec);
        if (StrUtils.isEmpty(releaseDate) || (!StrUtils.isEmpty(releaseDate) && releaseDate.startsWith("9"))) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Datensatz wurde DIALOG-seitig noch nicht freigegeben."),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            importer.reduceRecordCount();
            return false;
        }
        return true;
    }

    /**
     * Überprüft, ob die im übergebenen Baumuster enthaltene Baureihe versorgungsrelevant ist. Falls nicht, werden die entsprechenden Log-Ausgaben
     * in das MessageLog des übergebenen Importers geschrieben (samt reduceRecordCount() Aufruf)
     *
     * @param modelNumber
     * @param invalidSeriesSet
     * @param importer
     * @return
     */
    public boolean checkImportRelevanceForSeriesFromModel(String modelNumber, Set<String> invalidSeriesSet, AbstractDataImporter importer) {
        String seriesFromModel = iPartsDIALOGSeriesValidityCache.getInstance(getProject()).getExistingSeriesFromModel(modelNumber);
        if (StrUtils.isValid(seriesFromModel)) {
            return checkImportRelevanceForSeries(seriesFromModel, invalidSeriesSet, importer);
        }
        return false;
    }

    /**
     * Überprüft, ob die übergebene Baureihe versorgungsrelevant ist. Falls nicht, werden die entsprechenden Log-Ausgaben
     * in das MessageLog des übergebenen Importers geschrieben (samt reduceRecordCount() Aufruf)
     *
     * @param seriesNumber
     * @param invalidSeriesSet Enthält alle bisher geprüften und invaliden Baureihen. Im XML können mehrere Datensätze vorkommen, die sich auf eine Baureihe
     *                         beziehen, für die nichts importiert werden soll. Mit Hilfe des Sets werden mehrfache gleiche Log-Ausgaben verhindert
     * @param importer
     * @return
     */
    public boolean checkImportRelevanceForSeries(String seriesNumber, Set<String> invalidSeriesSet, AbstractDataImporter importer) {
        boolean validSeriesInImportData = true;
        boolean validImporter = importer != null;
        if (StrUtils.isValid(seriesNumber) && (invalidSeriesSet != null) && invalidSeriesSet.contains(seriesNumber)) {
            if (validImporter) {
                importer.reduceRecordCount();
            }
            return false;
        }

        if (StrUtils.isEmpty(seriesNumber)) {
            if (validImporter) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Datensatz enthält keine Baureihe und wird als \"nicht relevant für Import\" interpretiert!"),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                importer.reduceRecordCount();
            }
            validSeriesInImportData = false;
        } else if (!iPartsDIALOGSeriesValidityCache.getInstance(getProject()).seriesExists(seriesNumber)) {
            // LOG-Message nur 1x pro Baureihe ins Logfile schreiben.
            if (validImporter) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Baureihe %1 existiert nicht in der Datenbank und wird als \"nicht relevant für Import\" interpretiert!",
                                                                              seriesNumber),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                importer.reduceRecordCount();
            }
            if ((invalidSeriesSet != null)) {
                invalidSeriesSet.add(seriesNumber);
            }
            validSeriesInImportData = false;
        }

        if (!validSeriesInImportData) {
            return false;
        }

        boolean seriesIsRelevantForImport = iPartsDIALOGSeriesValidityCache.getInstance(getProject()).isSeriesValidForDIALOGImport(seriesNumber);
        if (!seriesIsRelevantForImport) {
            // LOG-Message nur 1x pro Baureihe ins Logfile schreiben.
            if (validImporter) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Baureihe %1 ist als \"nicht relevant für Import\" gekennzeichnet!", seriesNumber),
                                                     MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                importer.reduceRecordCount();
            }
            if ((invalidSeriesSet != null)) {
                invalidSeriesSet.add(seriesNumber);
            }
        }
        return seriesIsRelevantForImport;
    }

    /**
     * Überprüft, ob die in der übergebenen Farbtabellennummer enthaltene Baureihe versorgungsrelevant ist. Falls nicht, werden die entsprechenden Log-Ausgaben
     * in das MessageLog des übergebenen Importers geschrieben (samt reduceRecordCount() Aufruf)
     *
     * @param colorTableId
     * @param invalidSeriesSet
     * @param importer
     * @return
     */
    public boolean checkImportRelevanceForSeriesFromColortable(String colorTableId, Set<String> invalidSeriesSet, AbstractDataImporter importer) {
        return checkImportRelevanceForSeries(ColorTableHelper.extractSeriesNumberFromTableId(colorTableId), invalidSeriesSet, importer);
    }

    public String getDatasetKem(Map<String, String> importRec) {
        return getDataSetValue(iPartsTransferConst.ATTR_TABLE_KEM, importRec);
    }

    public String getDatabaseSeqNo(Map<String, String> importRec) {
        return getDataSetValue(iPartsTransferConst.ATTR_TABLE_SEQUENCE_NO, importRec);
    }

    /**
     * Überprüft, ob der übergebene Datensatz ein Löschkennzeichen ("L") enthält
     *
     * @param importRec
     * @return
     */
    public static boolean isDatasetMarkedForDeletion(Map<String, String> importRec) {
        return hasRecordGivenSDBValue(importRec, iPartsSDBValues.DELETE);
    }

    /**
     * Überprüft, ob der übergebene Datensatz ein Kennzeichen für ein folgendes Update ("B") enthält
     *
     * @param importRec
     * @return
     */
    public static boolean isDatasetMarkedForFollowingUpdate(Map<String, String> importRec) {
        return hasRecordGivenSDBValue(importRec, iPartsSDBValues.UPDATE_EXISTS);
    }

    public static boolean hasRecordGivenSDBValue(Map<String, String> importRec, iPartsSDBValues value) {
        iPartsSDBValues result = iPartsSDBValues.getFromOriginalValue(getDataSetValue(iPartsTransferConst.ATTR_TABLE_SDB_FLAG, importRec));
        return result == value;
    }

    private static String getDataSetValue(String keyValue, Map<String, String> importRec) {
        String value = importRec.get(keyValue);
        if (value == null) {
            value = "";
        }
        return value;
    }

    public iPartsDataReleaseState calculateReleaseStateByUsage(iPartsDialogBCTEPrimaryKey relevantBCTEPrimaryKey) {
        Set<iPartsDialogBCTEPrimaryKey> bcteKeyInSet = new HashSet<>(1);
        bcteKeyInSet.add(relevantBCTEPrimaryKey);
        return calculateReleaseStateByUsage(bcteKeyInSet);
    }

    public iPartsDataReleaseState calculateReleaseStateByUsage(Set<iPartsDialogBCTEPrimaryKey> relevantBCTEPrimaryKeys) {
        if (relevantBCTEPrimaryKeys.isEmpty()) {
            // Datensätze, deren Teile in keiner Stücklistenpositon (DA_DIALOG) verortet sind erhalten den Status "freigegeben"
            return iPartsDataReleaseState.RELEASED;
        }
        // Prüfen ob bereits in einer AS-Stückliste oder einem in Bearbeitung befindlichen Autoren-Auftrag verwendet
        // werden -> falls nein, wird der Status ebenfalls auf "freigegeben" gesetzt
        boolean isUsedInAS = false;
        for (iPartsDialogBCTEPrimaryKey bctePrimaryKey : relevantBCTEPrimaryKeys) {
            if (isUsedInASAndNonPSKProducts(bctePrimaryKey, true)) {
                isUsedInAS = true;
                break;
            }
        }
        if (!isUsedInAS) {
            return iPartsDataReleaseState.RELEASED;
        }
        return iPartsDataReleaseState.NEW;
    }

    /**
     * Hilfsmethode zum Überprüfen, ob die übergebene BCTE Position in AS verwendet wird. Optional kann angegeben werden,
     * ob offene Autorenaufträg ebenfalls durchsucht werden sollen.
     *
     * @param bctePrimaryKey
     * @param checkOpenAuthorOrders
     * @return
     */
    public boolean isUsedInAS(iPartsDialogBCTEPrimaryKey bctePrimaryKey, boolean checkOpenAuthorOrders) {
        if (checkOpenAuthorOrders) {
            return getAsUsageHelper().isUsedInAS(bctePrimaryKey);
        } else {
            return getAsUsageHelper().isUsedInASPartList(bctePrimaryKey);
        }
    }

    public boolean isUsedInASAndNonPSKProducts(iPartsDialogBCTEPrimaryKey bctePrimaryKey, boolean checkOpenAuthorOrders) {
        return isUsedInAS(bctePrimaryKey, checkOpenAuthorOrders) && !getAsUsageHelper().checkIfOnlyPSKProducts(bctePrimaryKey, checkOpenAuthorOrders);
    }

    /**
     * Änderungssatz für DIALOG-Änderungsdienst schreiben
     *
     * @param changeType Änderungstyp
     * @param changeId   {@link IdWithType} mit der ID des geänderten Datensatzes
     * @param seriesNo   Baureihennummer
     * @param bcteKey    BCTE-Schlüssel (dann ist <i>matNo</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param matNo      Materialnummer (dann ist <i>bcteKey</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param katalogId  die ID in der Katalog-Tabelle (immer in Kombination mit dem BCTE-Schlüssel)
     *                   (dann ist <i>matNo</i> {@code null} bzw. leer)
     */
    protected iPartsDataDIALOGChange createChangeRecord(iPartsDataDIALOGChange.ChangeType changeType, IdWithType changeId,
                                                        String seriesNo, String bcteKey, String matNo, String katalogId) {
        iPartsDialogChangesId dialogChangesId = new iPartsDialogChangesId(changeType, changeId, seriesNo, bcteKey, matNo, katalogId);
        iPartsDataDIALOGChange dataDialogChanges = new iPartsDataDIALOGChange(getProject(), dialogChangesId);
        if (!dataDialogChanges.existsInDB()) {
            dataDialogChanges.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataDialogChanges.setFieldValue(FIELD_DDC_SERIES_NO, seriesNo, DBActionOrigin.FROM_EDIT);   // für spätere mögliche Eingrenzung der Änderungen auf Baureihe
            dataDialogChanges.setFieldValue(FIELD_DDC_BCTE, bcteKey, DBActionOrigin.FROM_EDIT);
            dataDialogChanges.setFieldValue(FIELD_DDC_MATNR, matNo, DBActionOrigin.FROM_EDIT);
            dataDialogChanges.setFieldValue(FIELD_DDC_KATALOG_ID, katalogId, DBActionOrigin.FROM_EDIT);
        }
        return dataDialogChanges;
    }

    public void setHmMSmFields(EtkDataObject dataObject, Map<String, String> importRec, String seriesImportFieldname,
                               String hmmsmImportFieldname, String dbHmFieldname, String dbMFieldname, String dbSmFieldname) {
        // Die Längenüberprüfung findet bereits bei: isDialogRecordValid() statt!
        HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importRec.get(seriesImportFieldname), importRec.get(hmmsmImportFieldname));
        dataObject.setAttributeValue(dbHmFieldname, hmMSmId.getHm(), DBActionOrigin.FROM_EDIT);
        dataObject.setAttributeValue(dbMFieldname, hmMSmId.getM(), DBActionOrigin.FROM_EDIT);
        dataObject.setAttributeValue(dbSmFieldname, hmMSmId.getSm(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Setzt den Status abhängig von Importtyp. Handelt es sich nicht um einen Urladungsimporter, dann wird der
     * Default-Wert gesetzt.
     *
     * @param isInitialDataImport
     * @param dataObject
     * @param statusField
     */
    public void setDIALOGStateByImportTypeWithDefault(EtkDataObject dataObject, String statusField, boolean isInitialDataImport) {
        // Urladungsimporte erhalten immer den Status "freigegeben"
        if (isInitialDataImport) {
            dataObject.setFieldValue(statusField, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        } else {
            dataObject.setFieldValue(statusField, iPartsDataReleaseState.IMPORT_DEFAULT.getDbValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Löscht den Inhalt der Nicht-Schlüsselfelder des übergebenen {@link EtkDataObject}s, wenn die Quelle der
     * ursprünglichen Daten "MAD" ist
     *
     * @param dataObject
     * @param sourceField
     * @param setDIALOGSource
     * @return {@code true} falls der Inhalt der Nicht-Schlüsselfelder des übergebenen {@link EtkDataObject}s gelöscht wurde
     */
    public boolean deleteContentIfMADSource(EtkDataObject dataObject, String sourceField, boolean setDIALOGSource) {
        if (dataObject.existsInDB()) {
            if (iPartsImportDataOrigin.getTypeFromCode(dataObject.getFieldValue(sourceField)) == iPartsImportDataOrigin.MAD) {
                clearContent(dataObject);
                if (setDIALOGSource) {
                    dataObject.setFieldValue(sourceField, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Leert den Inhalt des übergebenen {@link EtkDataObject}.
     *
     * @param dataObject
     */
    // TODO: eventuell in EtkDataObject verschieben
    public void clearContent(EtkDataObject dataObject) {
        boolean exists = dataObject.existsInDB();
        dataObject.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        if (exists) {
            dataObject.__internal_setNew(false);
        }
    }

    /**
     * Importiert die PEM Stammdaten für alle PEMs, die während eines Imports eingesammelt wurden.
     *
     * @param project
     * @param importer
     */
    public static void importPEMMasterData(EtkProject project, AbstractDataImporter importer,
                                           Map<iPartsPemId, PEMDataHelper.PEMImportRecord> pemMasterData) {
        if (pemMasterData != null) {
            // Alle PEM Stammdaten durchlaufen
            for (PEMDataHelper.PEMImportRecord pemData : pemMasterData.values()) {
                iPartsDataPem dataPem = PEMDataHelper.createPEMDataObjectFromDIALOGImportRecord(project, pemData, null, true);
                if (dataPem != null) {
                    importer.saveToDB(dataPem);
                }
            }
        }
    }

    /**
     * Überprüft, ob die Stammdaten zur übergebenen PEM neuer sind als die bestehenden PEM Stammdaten
     * in <code>pemData</code>. Falls ja, wird pro PEM ab ein neuer PEM Stammdatensatz angelegt.
     *
     * @param pemData
     * @param importRec
     * @param changeDateFieldName
     * @param pemFromFieldName
     * @param pemFromDateFieldName
     * @param controlCodeFromFieldName
     * @param factoryFieldName
     */
    public void checkPEMData(Map<iPartsPemId, PEMDataHelper.PEMImportRecord> pemData, Map<String, String> importRec, String changeDateFieldName,
                             String pemFromFieldName, String pemFromDateFieldName, String controlCodeFromFieldName,
                             String factoryFieldName) {
        if (pemData != null) {
            // ADAT
            String changeDate = handleValueOfSpecialField(changeDateFieldName, importRec);
            // Werk
            String factory = handleValueOfSpecialField(factoryFieldName, importRec);
            // PEM ab
            String pem = handleValueOfSpecialField(pemFromFieldName, importRec);
            // PEM ab Datum
            String pemDate = handleValueOfSpecialField(pemFromDateFieldName, importRec);
            // Steuercode ab
            String controlCode = handleValueOfSpecialField(controlCodeFromFieldName, importRec);
            PEMDataHelper.addPemData(pemData, changeDate, pem, pemDate, controlCode, factory);
        }
    }
}