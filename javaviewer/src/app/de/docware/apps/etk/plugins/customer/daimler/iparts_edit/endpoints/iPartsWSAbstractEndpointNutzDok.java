/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.defaultconfig.webservice.WebserviceSettings;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectInterface;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.security.PasswordString;

/**
 * Abstrakte Klasse für iParts NutzDok-Endpoints.
 * <br/>Der Generic {@code REQUEST_CLASS} gibt die Klasse an, die für das Request-Objekt von diesem Endpoint erwartet und automatisch
 * aus einem JSON-String erzeugt wird. In anderen JAVA-Klassen heißen Generics meistens {@code <T>}.
 * Diese Klasse macht also die Annahme, dass der Request-Body genau ein JSON-Objekt enthält, welches zur Klasse REQUEST_CLASS
 * konvertiert werden kann.
 */
public abstract class iPartsWSAbstractEndpointNutzDok<REQUEST_CLASS extends WSRequestTransferObjectInterface> extends WSAbstractEndpoint<REQUEST_CLASS> {

    public enum CortexResult {
        OK_CONTINUE,    // nur WebService ausführen
        ERROR,          // Fehler aufgetreten
        OK_STOP         // nur Cortex-Algorithmus ohne WebService-Endpoint Logik
    }

    // Flag zum Speichern der Daten in der CORTEX Tabelle: Wenn true, dann werden die Daten der EndPoints in der
    // CORTEX Tabelle gespeichert
    public static final boolean SAVE_VIA_CORTEX_TABLE = true;
    // Flag zum direkten Verarbeiten des Requests. Wenn "false", wird nach dem Speichern in der CORTEX Tabelle der
    // Code des EndPoints NICHT durchlaufen
    private static final boolean EXECUTE_WEBSERVICE = false;

    protected iPartsNumberHelper numberHelper = new iPartsNumberHelper();

    public iPartsWSAbstractEndpointNutzDok(String endpointUri) {
        super(endpointUri, iPartsEditPlugin.LOG_CHANNEL_WS_DEBUG, iPartsEditPlugin.LOG_CHANNEL_WS_PERFORMANCE);
        logChannelSecure = iPartsEditPlugin.LOG_CHANNEL_WS_TOKEN;
    }

    @Override
    protected WebserviceSettings setConfiguredCacheSettings() {
        // die JavaViewer-WebserviceSettings werden bei iParts nicht ausgelesen. Stattdessen werden sie in in den
        // entsprechenden Plug-Ins definiert und hier ausgelesen. Bei NutzDok wird aber überhaupt nicht gecached.
        return null;
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        final String headerName = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_NAME);
        final String authorizationType = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_TYPE);

        // Passwort für HS256 Verfahren; wenn leer ist das Verfahren nicht zugelassen
        final PasswordString secret = iPartsEditPlugin.getPluginConfig().getConfigValueAsPassword(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_PASSWORD);

        // Token validieren
        final int expiryDelay = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_NUTZDOK_HEADER_TOKEN_EXPIRES);
        long currentTime = System.currentTimeMillis() / 1000;
        return isValidJWT(request, headerName, authorizationType, secret, currentTime, expiryDelay, iPartsEditPlugin.getKeystoreManagerNutzDok().getKeystores());
    }

    /**
     * Liefert die Retail-SAA-Nummer für die übergebene (formatierte) Konstruktions-SAA-Nummer zurück.
     *
     * @param saaNumber
     * @return
     */
    protected String getRetailSaaInDbFormat(String saaNumber) {
        saaNumber = numberHelper.unformatSaaForDB(saaNumber);

        // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
        String retailSaaNumber = numberHelper.getDifferentRetailSAA(saaNumber);
        if (retailSaaNumber != null) {
            return retailSaaNumber;
        } else {
            return saaNumber;
        }
    }

    /**
     * Erzeugt und speichert ein {@link iPartsDataCortexImport} DataObject zum aktuellen Request,
     * {@link iPartsCortexImportEndpointNames} Typ und {@link iPartsCortexImportMethod} Methode.
     *
     * @param project
     * @param logHelper
     * @param requestObject
     * @param endPointName
     * @param importMethod
     * @return
     */
    protected CortexResult createAndSaveCortexElement(EtkProject project, ImportExportLogHelper logHelper,
                                                      WSRequestTransferObject requestObject,
                                                      iPartsCortexImportEndpointNames endPointName,
                                                      iPartsCortexImportMethod importMethod) {
        // wenn der Cortex-Schaduler nicht aktiv ist => Standard-WebService-Endpoints
        if (!SAVE_VIA_CORTEX_TABLE) {
            return CortexResult.OK_CONTINUE;
        }
        GenericEtkDataObjectList<EtkDataObject> dataObjectToBeSaved = new GenericEtkDataObjectList<>();
        try {
            iPartsDataCortexImport dataCortexImport =
                    iPartsDataCortexImport.createNewDataCortexImport(project, endPointName,
                                                                     importMethod,
                                                                     getAsJSON(requestObject));
            dataObjectToBeSaved.add(dataCortexImport, DBActionOrigin.FROM_EDIT);
            saveInTransaction(project, dataObjectToBeSaved);
        } catch (RuntimeException e) {
            // Falls ein Fehler auftritt, diesen abfangen und weiterwerfen, damit man das Job-Log hier abbrechen kann.
            fireExceptionLogErrors(logHelper, null, e);
            throwError(HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR, WSError.INTERNAL_ERROR, "Error during saving CORTEX record", null);
            return CortexResult.ERROR;
        }
        if (EXECUTE_WEBSERVICE) {
            // trotz Cortex-Ablage auch die Logik des Webservice-Endpoint ausführen
            logHelper.addLogMsgWithTranslation("!!CORTEX Datensatz erfolgreich angelegt und Abarbeitung sofort " +
                                               "gestartet! Typ: \"%1\" und Methode \"%2\"", endPointName.getDBValue(), importMethod.getDBValue());
            return CortexResult.OK_CONTINUE;
        }
        logHelper.addLogMsgWithTranslation("!!CORTEX Datensatz erfolgreich angelegt! Typ: \"%1\" und Methode " +
                                           "\"%2\"", endPointName.getDBValue(), importMethod.getDBValue());
        return CortexResult.OK_STOP;
    }

    /**
     * Erzeugt aus dem übergebenen {@link SerializedDBDataObjectInterface} einen JSON-String.
     *
     * @param serializedDbDataObject
     * @return
     */
    protected String getAsJSON(WSRequestTransferObject serializedDbDataObject) {
        // Hier einen Genson nutzen, der die Variablen "securePayload" und "httpServerRequest" aus der Klasse
        // "WSRequestTransferObject" nicht serialisiert
        Genson genson = JSONUtils.createGensonWithOmittedFields(true, "securePayload", "httpServerRequest");
        return genson.serialize(serializedDbDataObject);
    }

    protected void fireExceptionLogErrors(ImportExportLogHelper logHelper, String logMessage, Throwable e) {
        if (StrUtils.isValid(logMessage)) {
            logHelper.addLogErrorWithTranslation(logMessage);
        } else {
            logHelper.addLogError(Utils.exceptionToString(e));
        }
        logHelper.fireLineSeparator();
        logHelper.addLogErrorWithTranslation("!!Fehler bei der Verarbeitung.");
    }

    /**
     * Schreibt eine Log-Meldung ins Log, wenn der DB Datensatz schon existiert
     *
     * @param dbObjectToBeSaved
     * @param logHelper
     * @return Unterscheidet sich der neue Datensart von dem in der DB existierenden Datensatz?
     */
    protected boolean logAlreadyExistsMessage(EtkDataObject dbObjectToBeSaved, ImportExportLogHelper logHelper, boolean hasStatus) {
        String message = logHelper.translateForLog("!!Der Datensatz '%1' existiert bereits in der Datenbank.",
                                                   dbObjectToBeSaved.getAsId().toStringForLogMessages());
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

    protected boolean initIfNotExists(EtkDataObject dataObject) {
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            return false;
        }
        return true;
    }

    public static void saveInTransaction(EtkProject project, GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved) {
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            dataObjectsToBeSaved.saveToDB(project);
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (RuntimeException e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            throw e;
        }
    }
}