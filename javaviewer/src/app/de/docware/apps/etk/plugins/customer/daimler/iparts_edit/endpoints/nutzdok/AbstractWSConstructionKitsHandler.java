/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import com.owlike.genson.stream.JsonStreamException;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsNutzDokRemarkId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditSortStringforSourceGUIDHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks.iPartsWSDeleteConstructionKitsRemarkRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketRemarkItem;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;

/**
 * Basis-Klasse für die Handler des Cortes Schedulers
 */
public abstract class AbstractWSConstructionKitsHandler implements iPartsConst {

    public static boolean IS_TEST_MODE = false; // true: abgearbeitete Cortex-Recs werden NICHT gelöscht,

    /**
     * Job-File des LogHelper schließen
     *
     * @param logHelper
     */
    public static void closeJobFile(ImportExportLogHelper logHelper) {
        iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
    }

    /**
     * Job-File des LogHelper als Fehlerhaft kennzeichnen und schließen
     *
     * @param logHelper
     */
    public static void closeJobFileWithError(ImportExportLogHelper logHelper) {
        iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
    }

    /**
     * Mehrere GenericEtkDataObjectList<EtkDataObject>-Listen in DB speichern
     *
     * @param project
     * @param dataObjectsToBeSavedList
     * @return
     */
    private boolean saveInTransaction(EtkProject project, GenericEtkDataObjectList<EtkDataObject>... dataObjectsToBeSavedList) {
        if (!saveToDB) {
            return true;
        }
        if ((dataObjectsToBeSavedList == null) || (dataObjectsToBeSavedList.length == 0)) {
            return true;
        }
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            for (GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved : dataObjectsToBeSavedList) {
                if ((dataObjectsToBeSaved != null) && (!dataObjectsToBeSaved.isEmpty() || !dataObjectsToBeSaved.getDeletedList().isEmpty())) {
                    dataObjectsToBeSaved.saveToDB(project);
                }
            }
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
            return true;
        } catch (RuntimeException e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            return false;
        }
    }


    private final EtkProject project;
    private final iPartsCortexImportEndpointNames importType;
    private boolean isExternalLogHelper;
    private ImportExportLogHelper logHelper;
    protected iPartsNumberHelper numberHelper;
    private GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved;
    private boolean executeResult;
    protected String errorLogMessage;
    private int warningCount;
    private int errorCount;
    private boolean saveToDB = true;

    /**
     * @param project
     * @param importType
     */
    public AbstractWSConstructionKitsHandler(EtkProject project, iPartsCortexImportEndpointNames importType) {
        this.project = project;
        this.importType = importType;
    }

    /**
     * schreibt alles in übergebene LogDatei (Schließen muss außerhalb vorgenommen werden)
     *
     * @param project
     * @param importType
     * @param logHelper
     */
    public AbstractWSConstructionKitsHandler(EtkProject project, iPartsCortexImportEndpointNames importType, ImportExportLogHelper logHelper) {
        this(project, importType);
        if (logHelper != null) {
            this.logHelper = logHelper;
            this.isExternalLogHelper = true;
        }
    }

    public EtkProject getProject() {
        return project;
    }

    public ImportExportLogHelper getLogHelper() {
        return logHelper;
    }

    protected boolean getExecuteResult() {
        return executeResult;
    }

    public GenericEtkDataObjectList<EtkDataObject> getDataObjectsToBeSaved() {
        return dataObjectsToBeSaved;
    }

    public int getLogWarnings() {
        return warningCount;
    }

    public int getLogErrors() {
        return errorCount;
    }

    /**
     * Laden der vorhandenen Elemente für einen EndpointNamen und Status NEW
     *
     * @return
     */
    public iPartsDataCortexImportList loadHandleList() {
        // Liste laden
        return iPartsDataCortexImportList.loadNewEntriesForEndpoint(getProject(), importType);
    }

    /**
     * Master-Routine für die Analyse und Abspeichern in DB
     *
     * @param handleList
     * @return
     */
    public boolean executeLogic(iPartsDataCortexImportList handleList) {
        // LogHelper anlegen (falls nicht extern gesetzt)
        doCreateLogHelper();
        errorLogMessage = "";
        // Initialisierung
        if (!init(handleList)) {
            // keine Elemente in der Cortex-Table gefungen
            // LogHelper und -File schlie0en (falls nicht extern gesetzt)
            doCloseJobFile();
            // damit die weitere Verarbeitung weiter geht
            return true;
        }
        // Helper für exception
        iPartsDataCortexImport currentDataCortexImport = null;
        // alle Vorgänge für einen EndpointNamen laufen in einer Transaction
        // somit ist das Anlegen und Löschen möglich
        project.getDbLayer().startTransaction();
        try {
            for (iPartsDataCortexImport dataCortexImport : handleList) {
                currentDataCortexImport = dataCortexImport;
                // zur Sicherheit den Blob laden
                dataCortexImport.loadAllExtendedDataTypeAttributes();
                // die eigentliche Ausführung für deinen Blob (JSON)
                executeResult = doExecute(dataCortexImport);
                boolean doBreak = false;
                if (!executeResult) {
                    // Status im Cortex Object setzen
                    dataCortexImport.setProcessingState(iPartsCortexImportProcessingState.ERROR, DBActionOrigin.FROM_EDIT);
                    addToSave(dataCortexImport);
                    doBreak = true;
                } else if (IS_TEST_MODE) {
                    if (dataCortexImport.getProcessingState() == iPartsCortexImportProcessingState.NEW) {
                        // beim Text den Status so setzen, dass die Cortex Elemente nicht gelöscht werden
                        dataCortexImport.setProcessingState(iPartsCortexImportProcessingState.UNKNOWN, DBActionOrigin.FROM_EDIT);
                    }
                    addToSave(dataCortexImport);
                } else {
                    // Fehlerfrei => Löschen des Cortes-DataObjects
                    addToDelete(dataCortexImport);
                }
                // Ergebniss für einen Blob (JSON) speichern (bleibt in der Transaction)
                doSaveInTransaction();
                if (doBreak) {
                    break;
                }
            }
            // falls nach der Abarbeitung für einen EndpointNamen  noch was zu tun ist
            doAfterExecute(handleList);
            if (executeResult) {
                // LogHelper und -File schlie0en (falls nicht extern gesetzt)
                doCloseJobFile();
            } else {
                // gesammelte Fehlermeldung ausgeben
                fireExceptionLogErrors(errorLogMessage, null);
                // LogHelper und -File als fehlerhaft kennzeichnen und schlie0en (falls nicht extern gesetzt)
                doCloseJobFileWithError();
                return false;
            }
        } catch (RuntimeException e) {
            // Falls ein Fehler auftritt, diesen abfangen und weiterwerfen, damit man das Job-Log hier abbrechen kann.
            fireExceptionLogErrors(errorLogMessage, e);
            // LogHelper und -File als fehlerhaft kennzeichnen und schlie0en (falls nicht extern gesetzt)
            doCloseJobFileWithError();
            executeResult = false;
            // Aktuelles Cortex-DataObject als fehlerhaft kennzeichnen
            if (currentDataCortexImport != null) {
                currentDataCortexImport.setProcessingState(iPartsCortexImportProcessingState.ERROR, DBActionOrigin.FROM_EDIT);
            }
            // die bisherigen Ergenissse und vorallem currentDataCortexImport speichern
            addToSave(currentDataCortexImport);
            // falls nach der Abarbeitung im Fehlerfall noch was zu tun ist
            doAfterExecute(handleList);
            // bisherige Ergebnisse speichern
            doSaveInTransaction();
            return false;
        } finally {
            // Transaction für alle Elemente eines EndpointNamens schließen
            project.getDbLayer().commit();
        }
        return true;
    }

    /**
     * @return
     */
    protected abstract boolean doBeforeLogic();

    /**
     * Initialisierung der benötigten Daten
     * Überprüfung, ob für einen EndpointNamen Daten vorhanden sind
     *
     * @param handleList
     * @return
     */
    protected boolean init(iPartsDataCortexImportList handleList) {
        executeResult = true;
        numberHelper = new iPartsNumberHelper();
        dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
        warningCount = 0;
        errorCount = 0;
        boolean result = !handleList.isEmpty();
        if (!result) {
            getLogHelper().addLogMsgWithTranslation("!!Keine Elemente zum Importieren");
        }
        return result;
    }

    /**
     * Die eigentliche Ausführung für jeden Handler
     *
     * @param dataChangeSetEntry
     * @return
     */
    protected abstract boolean doExecute(iPartsDataCortexImport dataChangeSetEntry);

    /**
     * Erfolgs-/Misserfolgs-Meldung ausgeben
     *
     * @param dataCortexImport
     * @param result
     * @return
     */
    protected boolean addLogMsgAfterDoExecute(iPartsDataCortexImport dataCortexImport, boolean result) {
        if (result) {
            getLogHelper().addLogMsgWithTranslation("!!Cortex-Element vom %1 importiert.", dataCortexImport.getAsId().getCreationDate());
        } else {
            addLogWarning("!!Cortex-Element vom %1 nicht importiert.", dataCortexImport.getAsId().getCreationDate());
        }
        return result;
    }

    /**
     * Falls nach den Execute noch was zu tun ist
     *
     * @param handleList
     */
    protected void doAfterExecute(iPartsDataCortexImportList handleList) {
    }

    /**
     * EtkDataObject als zum Löschen speichern
     *
     * @param deleteObject
     */
    protected void doDeleteObject(EtkDataObject deleteObject) {
        addToDelete(deleteObject);
    }

    /**
     * EtkDataObject als zum Speichern speichern
     *
     * @param dataObject
     */
    protected void addToSave(EtkDataObject dataObject) {
        dataObjectsToBeSaved.add(dataObject, DBActionOrigin.FROM_EDIT);
    }

    /**
     * EtkDataObject als zum Löschen speichern
     *
     * @param dataObject
     */
    protected void addToDelete(EtkDataObject dataObject) {
        dataObjectsToBeSaved.delete(dataObject, true, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Hilfsroutine zum Überprüfen der gültigen SAA-Nummer
     * Bei Fehler wird dies im LogFile vermerkt, die Abarbeitung geht aber weiter
     *
     * @param saaNumber
     * @return
     */
    protected String getSaaNoInDbFormat(String saaNumber) {
        String saaNo;
        // getSaaNoInDbFormat() wirft eine Exception, wenn das Format der SAA-Nummer den Vorgaben nicht entspricht.
        // Diese Exception muss abgefangen werden
        try {
            saaNo = getRetailSaaInDbFormat(saaNumber);
        } catch (RuntimeException e) {
            if (StrUtils.isValid(e.getMessage())) {
                addLogError(e.getMessage());
            } else {
                addLogError("!!Ungültige SAA-Nummer \"%1\"", saaNumber);
            }
            // mit dem nächsten weitermachen
            addLogWarning("!!Cortex Datensatz wird ignoriert");
            return null;
        }
        return saaNo;
    }

    /**
     * Liefert die Retail-SAA-Nummer für die übergebene (formatierte) Konstruktions-SAA-Nummer zurück.
     *
     * @param saaNumber
     * @return
     */
    protected String getRetailSaaInDbFormat(String saaNumber) {
        saaNumber = numberHelper.unformatSaaForDB(saaNumber);

        // Abweichende Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
        String retailSaaNumber = numberHelper.getDifferentRetailSAA(saaNumber);
        if (StrUtils.isValid(retailSaaNumber)) {
            return retailSaaNumber;
        } else {
            return saaNumber;
        }
    }

    protected String getTimeZoneDateAsISODate(String timeZoneDate) {
        if (StrUtils.isValid(timeZoneDate)) {
            // DateTime wird in Cortex als yyyymmddhhmmss geliefert (entspricht somit dem DB-Format)
            if (!timeZoneDate.contains("-")) {
                return timeZoneDate;
            }
            return XMLImportExportDateHelper.getTimeZoneDateAsISODate(timeZoneDate);
        }
        return "";
    }

    /**
     * Für die Überprüfung und Wandlung des Datums in den Annotation
     *
     * @param date
     * @return
     */
    protected String getDateFromAnnotation(String date) {
        if (StrUtils.isValid(date)) {
            // Date wird in Cortex als yyyy-mm-dd bei der Annotation geliefert
            if (!date.contains("-")) {
                return EditSortStringforSourceGUIDHelper.rightFill(date, DateUtils.simpleTimeFormatyyyyMMddHHmmss.length(), '0');
            }
            try {
                return EditSortStringforSourceGUIDHelper.rightFill(DateUtils.toyyyyMMdd_Calendar(DateUtils.toCalendar_ISO(date)),
                                                                   DateUtils.simpleTimeFormatyyyyMMddHHmmss.length(), '0');
            } catch (DateException | ParseException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR,
                                                   new RuntimeException("Can not parse string as date with optional time zone."
                                                                        + " String value: " + date));
            }
        }
        return "";
    }

    /**
     * Überlagerte Routine zum Anlegen des LogHelper, falls nicht extern gesetzt
     */
    protected void doCreateLogHelper() {
        if (logHelper == null) {
            logHelper = ImportExportLogHelper.createLogHelperWithRunningJob(importType.getJobTypeName());
        }
        // Start Meldung ausgeben
        doFireStartMsg();
    }

    /**
     * Startmeldung ausgeben
     */
    protected void doFireStartMsg() {
        logHelper.fireLineSeparator();
        logHelper.addLogMsgWithTranslation("!!Cortex Import für \"%1\" gestartet.", importType.getJobTypeName());
    }

    /**
     * Endemeldung ausgeben
     */
    protected void doFireEndMsg() {
        String withError = "";
        if (!executeResult) {
            withError = logHelper.translateForLog("!!mit Fehler");
        }
        logHelper.addLogMsgWithTranslation("!!Cortex Import für \"%1\" %2 beendet.", importType.getJobTypeName(), withError);
        logHelper.fireLineSeparator();
        logHelper.addNewLine();
    }

    /**
     * Überlagerte Routine zum Schließen der LogDatei, falls nicht extern gesetzt
     */
    protected void doCloseJobFile() {
        // Endemeldung ausgeben
        doFireEndMsg();
        if (!isExternalLogHelper) {
            iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
        }
    }

    /**
     * Überlagerte Routine zum Schließen mit Fehler der LogDatei, falls nicht extern gesetzt
     */
    protected void doCloseJobFileWithError() {
        // Endemeldung ausgeben
        doFireEndMsg();
        if (!isExternalLogHelper) {
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
        }
    }

    /**
     * Entweder die logMessage oder die Exception in die LogDatei scheiben
     *
     * @param logMessage
     * @param e
     */
    protected void fireExceptionLogErrors(String logMessage, Throwable e) {
        if (StrUtils.isValid(logMessage)) {
            addLogError(logMessage);
        } else if (e != null) {
            addLogError(Utils.exceptionToString(e));
        }
        logHelper.fireLineSeparator();
        logHelper.addLogErrorWithTranslation("!!Fehler bei der Verarbeitung.");
    }

    /**
     * EtkDataObject initialisieren, falls nicht in der DB vorhanden
     *
     * @param dataObject
     * @return
     */
    protected boolean initIfNotExists(EtkDataObject dataObject) {
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            return false;
        }
        return true;
    }

    /**
     * Schreibt eine Logmeldung ins Log, wenn der DB Datensatz schon existiert
     *
     * @param dbObjectToBeSaved
     * @return Unterscheidet sich der neue Datensart von dem in der DB existierenden Datensatz?
     */
    protected boolean logAlreadyExistsMessage(EtkDataObject dbObjectToBeSaved, boolean hasStatus) {
        String message = logHelper.translateForLog("!!Der Datensatz %1 aus %2 existiert bereits in der Datenbank.",
                                                   dbObjectToBeSaved.getAsId().toStringForLogMessages(), dbObjectToBeSaved.getTableName());
        boolean modifiedWithChildren = dbObjectToBeSaved.isModifiedWithChildren();
        if (modifiedWithChildren) {
            message += " ";
            if (hasStatus) {
                message += logHelper.translateForLog("!!Dieser unterscheidet sich von dem zu importierenden Datensatz."
                                                     + " Alle Felder werden überschrieben, nur der Status bleibt erhalten.");
            } else {
                message += logHelper.translateForLog("!!Dieser unterscheidet sich von dem zu importierenden Datensatz."
                                                     + " Alle Felder werden überschrieben.");
            }
        }
        logHelper.addLogMsg(message);
        return modifiedWithChildren;
    }

    /**
     * Überlagerte Funktion zum Speichern von DataObjects innerhalb der Transaction
     */
    protected void doSaveInTransaction() {
        saveInTransaction(project, dataObjectsToBeSaved);
        dataObjectsToBeSaved.clear(DBActionOrigin.FROM_DB);
    }

    protected void addLogWarning(String message, String... placeHolderTexts) {
        logHelper.addLogWarningWithTranslation(message, placeHolderTexts);
        warningCount++;
    }

    protected void addLogError(String message, String... placeHolderTexts) {
        logHelper.addLogErrorWithTranslation(message, placeHolderTexts);
        errorCount++;
    }

    /**
     * Deserialisierung des Blobs (JSON) eines Cortex-Datensatzes in ein Transfer-Object
     *
     * @param dataCortexImport
     * @param serializedClass
     * @return
     */
    protected <T extends WSRequestTransferObject> T getWSObject(iPartsDataCortexImport dataCortexImport,
                                                                Class<T> serializedClass) {
        String currentJSONData = getJSONData(dataCortexImport);
        if (StrUtils.isValid(currentJSONData)) {
            return getObjectFromJSON(currentJSONData, serializedClass);
        }
        return null;
    }

    /**
     * Blob (JSON) aus einem Cortex-Record holen
     *
     * @param dataCortexImport
     * @return
     */
    protected String getJSONData(iPartsDataCortexImport dataCortexImport) {
        try {
            return dataCortexImport.getCurrentData();
        } catch (Exception e) {
            errorLogMessage = TranslationHandler.translate("Error while loading zipped JSON for field \"%1\"",
                                                           FIELD_DCI_DATA);
            return null;
        }
    }

    /**
     * Deserialisierung eines JSON Strings in ein Transfer-Object
     *
     * @param jsonString
     * @param serializedClass
     * @return
     */
    protected <T extends WSRequestTransferObject> T getObjectFromJSON(String jsonString, Class<T> serializedClass) {
        Genson genson = JSONUtils.createGenson(false);
        try {
            return genson.deserialize(jsonString, serializedClass);
        } catch (JsonStreamException | JsonBindingException e) {
            errorLogMessage = TranslationHandler.translate("Deserialization of data \"%1\" to \"%2\" failed",
                                                           FIELD_DCI_DATA, serializedClass.getName());
            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_WS_DEBUG, LogType.ERROR, e);
            return null;
        }
    }
//===========================================================================================

    /**
     * Behandelt das Einfügen/Löschen eines RemarkItems für KEM/SAA
     *
     * @param dataCortexImport
     * @param type
     * @return
     */
    protected boolean handleInsertDeleteKitsRemarkItem(iPartsDataCortexImport dataCortexImport, iPartsWSWorkBasketItem.TYPE type) {
        if (dataCortexImport.getImportMethod() == iPartsCortexImportMethod.DELETE) {
            return handleDeleteKitsRemarkItem(dataCortexImport, type);
        } else if (dataCortexImport.getImportMethod() == iPartsCortexImportMethod.INSERT) {
            return handleInsertKitsRemarkItem(dataCortexImport, type);
        } else {
            // Fehlermeldung
            addLogError("Ungültige Import Methode des Cortex-Records \"%1\"!", dataCortexImport.getFieldValue(FIELD_DCI_IMPORT_METHOD));
            return false;
        }
    }

    /**
     * Behandelt das Löschen eines RemarkItems für KEM/SAA
     *
     * @param dataCortexImport
     * @param type
     * @return
     */
    protected boolean handleDeleteKitsRemarkItem(iPartsDataCortexImport dataCortexImport, iPartsWSWorkBasketItem.TYPE type) {
        // ein Lösch-Auftrag
        // aus dem Blob des Cortex-Objects das Transfer-Object bilden
        iPartsWSDeleteConstructionKitsRemarkRequest requestObject = getWSObject(dataCortexImport,
                                                                                iPartsWSDeleteConstructionKitsRemarkRequest.class);
        if ((requestObject == null) || !StrUtils.isValid(requestObject.getParentId())) {
            addLogError("Ungültiger Request!");
            return false;
        }
        String parentId = requestObject.getParentId();
        parentId = checkSaaOrKemNumber(dataCortexImport, parentId, type);
        if (parentId == null) {
            // mit dem nächsten weitermachen
            return true;
        }

        boolean result = handleWorkBasketRemarkItemKemOrSaaDelete(requestObject, parentId, type);
        return addLogMsgAfterDoExecute(dataCortexImport, result);
    }

    /**
     * Behandelt das Einfügen eines RemarkItems für KEM/SAA
     *
     * @param dataCortexImport
     * @param type
     * @return
     */
    protected boolean handleInsertKitsRemarkItem(iPartsDataCortexImport dataCortexImport, iPartsWSWorkBasketItem.TYPE type) {
        // ein Insert-Auftrag
        // aus dem Blob des Cortex-Objects das Transferobject bilden
        iPartsWSWorkBasketRemarkItem requestObject = getWSObject(dataCortexImport,
                                                                 iPartsWSWorkBasketRemarkItem.class);
        if ((requestObject == null) || StrUtils.isEmpty(requestObject.getRefId())) {
            addLogError("Ungültiger Request!");
            return false;
        }
        // Sonderbehandlung für SAA-Number
        String saaOrKemNo = requestObject.getRefId();
        // zur Sicherheit
        if (requestObject.getTypeAsEnum() != type) {
            // mit dem nächsten weitermachen
            addLogWarning("!!Unterschiedliche Typen im Request und dem Cortex Datensatz!");
            // Status im Cortex Object setzen
            dataCortexImport.setProcessingState(iPartsCortexImportProcessingState.ERROR, DBActionOrigin.FROM_EDIT);
            return true;
        }
        saaOrKemNo = checkSaaOrKemNumber(dataCortexImport, saaOrKemNo, type);
        if (saaOrKemNo == null) {
            // mit dem nächsten weitermachen
            return true;
        }

        iPartsDataNutzDokRemark nutzDokRemark = handleWorkBasketRemarkItem(requestObject, saaOrKemNo);
        String finalMessage;
        if (nutzDokRemark != null) {
            addToSave(nutzDokRemark);
            finalMessage = "!!Import erfolgreich. Es wurde eine Bemerkung importiert.";
        } else {
            finalMessage = "!!Import erfolgreich. Es wurde keine neue oder veränderte Bemerkung importiert.";
        }
        getLogHelper().addLogMsgWithTranslation(finalMessage);
        return addLogMsgAfterDoExecute(dataCortexImport, true);
    }

    /**
     * Überprüft eine übergebene SAA- oder KEM-Nummer
     *
     * @param dataCortexImport
     * @param saaOrKemNo
     * @param type
     * @return
     */
    protected String checkSaaOrKemNumber(iPartsDataCortexImport dataCortexImport, String saaOrKemNo,
                                         iPartsWSWorkBasketItem.TYPE type) {
        if (type == iPartsWSWorkBasketItem.TYPE.SAA) {
            // Korrekte SAA-Nummer in DB Format erhalten
            saaOrKemNo = getSaaNoInDbFormat(saaOrKemNo);
        }
        // SAA- und KEM-Nummer überprüfen
        if (!StrUtils.isValid(saaOrKemNo)) {
            addLogError("!!Ungültige %1-Nummer!", type.toString());
            // mit dem nächsten weitermachen
            getLogHelper().addLogMsgWithTranslation("!!Cortex Datensatz \"%1\" wird als fehlerhaft gekennzeichnet und ignoriert!", dataCortexImport.getAsId().getCreationDate());
            // Status im Cortex Object setzen
            dataCortexImport.setProcessingState(iPartsCortexImportProcessingState.ERROR, DBActionOrigin.FROM_EDIT);
            return null;
        }
        return saaOrKemNo;
    }

    /**
     * Erzeugt aus dem übergebenen Bemerkungs-Objekt {@link iPartsWSWorkBasketRemarkItem} ein Bemerkungs-Datenbankobjekt {@link iPartsDataNutzDokRemark}
     *
     * @param workBasketRemarkItem
     * @return
     */
    protected iPartsDataNutzDokRemark handleWorkBasketRemarkItem(iPartsWSWorkBasketRemarkItem workBasketRemarkItem, String saaOrKemNo) {
        iPartsNutzDokRemarkId nutzDokRemarkId = new iPartsNutzDokRemarkId(saaOrKemNo, workBasketRemarkItem.getType(),
                                                                          workBasketRemarkItem.getId());
        iPartsDataNutzDokRemark nutzDokRemark = new iPartsDataNutzDokRemark(getProject(), nutzDokRemarkId);
        boolean existsInDB = initIfNotExists(nutzDokRemark);
        fillNutzDokRemarkAttributes(workBasketRemarkItem, nutzDokRemark);
        if (existsInDB) {
            if (!logAlreadyExistsMessage(nutzDokRemark, false)) {
                return null;
            }
        }
        // KEM- & SAA-Arbeitsvorräte prüfen
        boolean workBasketItemFound = checkForKEMSAAWorkBasket(workBasketRemarkItem.getTypeAsEnum(), saaOrKemNo);
        String workBasketItemNotFoundMessage = null;
        if (!workBasketItemFound) {
            workBasketItemNotFoundMessage = getLogHelper().translateForLog("!!Es existiert kein Arbeitsauftrag für die %1 \"%2\".",
                                                                           workBasketRemarkItem.getType(), saaOrKemNo);
        }

        if (workBasketItemNotFoundMessage != null) {
            workBasketItemNotFoundMessage += " " + getLogHelper().translateForLog("!!Die Bemerkung wird trotzdem importiert.");
            addLogWarning(workBasketItemNotFoundMessage);
        }

        return nutzDokRemark;
    }

    /**
     * Prüft ob KEM- oder SAA-Arbeitsaufträge vorhanden sind
     *
     * @param workBasketRemarkItemType
     * @param kemOrSaaNo
     * @return
     */
    private boolean checkForKEMSAAWorkBasket(iPartsWSWorkBasketItem.TYPE workBasketRemarkItemType, String kemOrSaaNo) {
        // Prüfen, ob es überhaupt einen Arbeitsauftrag für die SAA bzw. KEM gibt
        boolean workBasketItemFound = false;
        if (workBasketRemarkItemType == iPartsWSWorkBasketItem.TYPE.KEM) {
            // KEM Arbeitsvorrat
            iPartsNutzDokKEMId nutzDokKEMId = new iPartsNutzDokKEMId(kemOrSaaNo);
            iPartsDataNutzDokKEM nutzDokKEM = new iPartsDataNutzDokKEM(getProject(), nutzDokKEMId);
            if (nutzDokKEM.existsInDB()) {
                workBasketItemFound = true;
            }
        } else if (workBasketRemarkItemType == iPartsWSWorkBasketItem.TYPE.SAA) {
            // SAA Arbeitsvorrat
            iPartsNutzDokSAAId nutzDokSAAId = new iPartsNutzDokSAAId(kemOrSaaNo);
            iPartsDataNutzDokSAA nutzDokSAA = new iPartsDataNutzDokSAA(getProject(), nutzDokSAAId);
            if (nutzDokSAA.existsInDB()) {
                workBasketItemFound = true;
            }
        } else {
            // Kann wegen der Validierung gar nicht passieren.
            addLogError("!!Unbekannetr Typ: \"%1\".", workBasketRemarkItemType.toString());
        }
        return workBasketItemFound;
    }


    /**
     * Befüllt das Bemerkungs-Datenbank-Objekt mit den Informationen aus der Bemerkung {@link iPartsWSWorkBasketRemarkItem}
     *
     * @param workBasketRemarkItem
     * @param nutzDokRemark
     */
    private void fillNutzDokRemarkAttributes(iPartsWSWorkBasketRemarkItem
                                                     workBasketRemarkItem, iPartsDataNutzDokRemark nutzDokRemark) {
        nutzDokRemark.setFieldValue(FIELD_DNR_LAST_USER, WSHelper.getEmptyStringForNull(workBasketRemarkItem.getUser()),
                                    DBActionOrigin.FROM_EDIT);
        nutzDokRemark.setFieldValue(FIELD_DNR_LAST_MODIFIED, XMLImportExportDateHelper.getTimeZoneDateAsISODate(workBasketRemarkItem.getUpdateTs()),
                                    DBActionOrigin.FROM_EDIT);

        // Bemerkung als gezippten BLOB abspeichern und vorher den existierenden BLOB explizit laden falls die DataObject
        // nicht neu ist, damit der Vergleich korrekt stattfinden kann
        if (!nutzDokRemark.isNew()) {
            nutzDokRemark.getFieldValueAsBlob(FIELD_DNR_REMARK);
        }
        nutzDokRemark.setFieldValueAsZippedBlob(FIELD_DNR_REMARK, workBasketRemarkItem.getDataAsBLOB(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Gemeinsame Routine für das Löschen eines KEM- oder SAA-Remarks
     *
     * @param requestObject
     * @param kemOrSaaNo
     * @param type
     * @return
     */
    protected boolean handleWorkBasketRemarkItemKemOrSaaDelete(iPartsWSDeleteConstructionKitsRemarkRequest requestObject,
                                                               String kemOrSaaNo, iPartsWSWorkBasketItem.TYPE type) {
        String identifier = "KEM";
        if (type == iPartsWSWorkBasketItem.TYPE.SAA) {
            identifier = "SAA";
        }
        iPartsNutzDokRemarkId remarkId = new iPartsNutzDokRemarkId(kemOrSaaNo, requestObject.getItemType().name(), requestObject.getRemarkId());
        iPartsDataNutzDokRemark remark = new iPartsDataNutzDokRemark(getProject(), remarkId);
        if (!remark.existsInDB()) {
            // Falls der Bemerkungstext nicht gefunden wurde, zunächst überprüfen, ob überhaupt einen Arbeitsauftrag
            // für die KEM oder SAA in der DB existiert
            boolean workBasketItemFound = checkForKEMSAAWorkBasket(type, kemOrSaaNo);
            if (!workBasketItemFound) {
                // kein Fehler, sondern nur Warnung
                addLogWarning("!!Es existiert kein Arbeitsauftrag für die %1 \"%2\".", identifier, kemOrSaaNo);
            } else {
                // Falls ein Arbeitsauftrag für die KEM/SAA in der DB existiert, gibt es die Bemerkungstext-ID nicht
                // kein Fehler, sondern nur Warnung
                addLogWarning("!!Keine Bemerkung für %1 \"%2\" und Bemerkungs-ID \"%3\" gefunden.",
                              identifier, kemOrSaaNo, requestObject.getRemarkId());
            }
        } else {
            // Hier wird gelöscht ...
            doDeleteObject(remark);
            // ... und ohne noch einmal zu lesen angenommen, dass das Löschen geklappt hat.
            getLogHelper().addLogMsgWithTranslation("!!Bemerkung für %1 \"%2\" und Bemerkungs-ID \"%3\" erfolgreich gelöscht.",
                                                    identifier, kemOrSaaNo, requestObject.getRemarkId());
        }
        return true;
    }

}
