/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper;

import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchIndexMetaData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchOperationResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.error.ElasticSearchErrorResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportExecutor;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportMessageFileHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallRESTFulWebserviceHelper;
import de.docware.framework.modules.webservice.restful.RESTfulException;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class FastSearchConnectionHelper {

    private enum OperationResult {
        OPERATION_FAILED,
        OPERATION_FAILED_WITH_EXCEPTION,
        INDEX_DOES_NOT_EXIST,
        RESPONSE_NOT_PARSABLE,
        ENDPOINT_CREATION_FAILED,
        OPERATION_SUCCESSFUL,
        SERVER_UNREACHABLE
    }

    private final CallRESTFulWebserviceHelper helper;
    private final Genson genson;
    private String currentBaseURL;
    private List<String> allBaseURLs;
    private boolean isTestModeActive;

    public FastSearchConnectionHelper() {
        this.helper = new CallRESTFulWebserviceHelper(true);
        this.genson = JSONUtils.createGenson(true);
        this.isTestModeActive = iPartsExportPlugin.isFastSearchTestModeActive();
        initBaseURLs();
    }

    /**
     * Aktualisiert alle Baumuster Daten zu einer Sprache.
     * Ablauf:
     * 1. Quell-Index für das aktuelle Produkt anlegen und Schreiboperation auf dem Index erlauben (block.write = false).
     * Falls der Index existiert, löschen.
     * 2. Schreiboperation auf dem Ziel-Index erlauben (block.write = false) bzw. Anlegen, wenn nicht existiert
     * 3. Pro Baumuster NDJSON eine bulk Anfrage abfeuern (optional kann nach einer eingestellten BM Anzahl ein _refresh
     * durchgeführt werden)
     * 4. Quell-Index aktualisieren
     * 5. Baumuster-Daten zu den übergebenen Baumuster aus dem jeweiligen Ziel-Index löschen
     * 6. Reindex durchführen (Daten von Quell-Index zu Ziel-Index transferieren)
     * 7. Schreiboperation auf dem Ziel-Index blockieren (block.write = true)  und Refresh auf 60s setzen
     * 8. Quell-Index löschen
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @param builderForOneModel
     * @return
     */
    private OperationResult handleAllModelsForLanguageWithReindex(iPartsProductId productId, String language,
                                                                  MultiThreadExportMessageFileHelper messageHelper,
                                                                  Map<String, StringBuilder> builderForOneModel) {
        // Quell-Index für das aktuelle Produkt anlegen und Schreiboperation auf dem Index erlauben (block.write = false).
        // Falls der Index existiert, löschen.
        OperationResult result = checkSourceIndex(productId, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich -> raus
            return result;
        }

        // Schreiboperation auf dem Ziel-Index erlauben (block.write = false) bzw. Anlegen, wenn er nicht existiert
        result = checkTargetIndex(productId, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich -> raus
            return result;
        }

        // Pro Baumuster NDJSON eine bulk Anfrage abfeuern und den Produkt Index befüllen
        String endpointURI = createBulkEndpointURI(language, productId);
        if (StrUtils.isEmpty(endpointURI)) {
            messageHelper.logError("Could not create bulk endpointURI for language " + language);
            return OperationResult.ENDPOINT_CREATION_FAILED;
        }
        String refreshEndpointURI = createProductIndexName(productId, language) + "/_refresh";
        result = sendProductBulkData(builderForOneModel, null, language, endpointURI, refreshEndpointURI,
                                     messageHelper, true);
        if (result == OperationResult.SERVER_UNREACHABLE) {
            // Verbindung zum Server ist bei einer Abfrage abgebrochen -> raus und Produkt nochmals versorgen
            return result;
        }

        // Nur, wenn alle BM erfolgreich hochgeladen wurden, den Ziel-Index befüllen
        if (result == OperationResult.OPERATION_SUCCESSFUL) {
            // Quell-Index aktualisieren
            result = refreshIndex(language, refreshEndpointURI, messageHelper);
            if (operationResultNotValid(result)) {
                // Operation war nicht erfolgreich -> raus
                return result;
            }

            // BM Daten aus dem Ziel-Index löschen
            Set<String> deletedModels = deleteModelDataFromIndex(productId, builderForOneModel, language, messageHelper);
            if (deletedModels == null) {
                // Verbindung zum Server abgebrochen -> Daten neu versorgen
                return OperationResult.SERVER_UNREACHABLE;
            }

            // Reindex durchführen (Daten von Quell-Index zu Ziel-Index transferieren)
            result = doReindex(productId, language, messageHelper);
            if (operationResultNotValid(result)) {
                // Operation war nicht erfolgreich -> raus
                return result;
            }
            // Ziel-Index aktualisieren
            endpointURI = createRefreshEndpointURI(language);
            if (operationResultNotValid(refreshIndex(language, endpointURI, messageHelper))) {
                // Aktualisieren war nicht erfolgreich -> Fehlermeldung und danach versuchen das Schreiben zu blockieren
                messageHelper.logMsg("Error while refreshing data for language " + language);
            }
        } else {
            messageHelper.logError("Error while updating models for " + productId.getProductNumber() + " and language "
                                   + language + ". At least one request was invalid!");
        }

        // Schreiboperation auf dem Ziel-Index blockieren (block.write = true) und Refresh auf 60s setzen
        result = changeIndexWriteOperation(productId, true, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich und der INDEX ist nicht neu -> raus
            return result;
        }

        // Quell-Index löschen
        result = deleteCompleteProductIndex(productId, language, messageHelper);
        return result;
    }

    /**
     * Überprüft, ob der Ziel-Index existiert und erlaubt das Schreiben von Daten via _settings Aufruf. Sollte er noch
     * nicht existieren, wird der Ziel-Index angelegt
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult checkTargetIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        // Ziel-Index zum Beschreiben aktivieren
        OperationResult result = changeIndexWriteOperation(productId, false, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich -> raus
            return result;
        }

        if (result == OperationResult.INDEX_DOES_NOT_EXIST) {
            // Ziel-Index existiert nicht -> anlegen
            // TODO: Hier den Ziel-Index mit den richtigen Settings anlegen (zukünftige Story)
            result = createTargetIndex(productId, language, messageHelper);
            if (operationResultNotValid(result)) {
                // Operation war nicht erfolgreich -> raus
                return result;
            }
        }
        return result;
    }

    /**
     * Legt einen Quell-Index an. Sillte er auch einem vorherigen Lauf schon existieren, wird die alte Version gelöscht
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult checkSourceIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        // Alte Version des Quell-Index, der nur für das Produkt angelegt wurde zur Sicherheit löschen
        OperationResult result = deleteCompleteProductIndex(productId, language, messageHelper);
        if (operationResultNotValid(result)) {
            return result;
        }
        // Quell-Index existiert nicht -> anlegen
        // TODO: Hier den Quell-Index mit den richtigen Settings anlegen (zukünftige Story)
        result = createSourceIndex(productId, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich -> raus
            return result;
        }

        return result;
    }

    /**
     * Legt einen neuen Quell-Index für die Sprache und das übergebene Produkt an
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult createSourceIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        String endpointURI = createProductIndexName(productId, language);
        return createNewIndex(productId, endpointURI, language, messageHelper);
    }

    /**
     * Legt einen neuen Ziel-Index für die Sprache an
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult createTargetIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        String endpointURI = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        return createNewIndex(productId, endpointURI, language, messageHelper);
    }

    /**
     * Legt einen neuen Index für die übergebene <code>endpointURI</code> an
     *
     * @param productId
     * @param endpointURI
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult createNewIndex(iPartsProductId productId, String endpointURI, String language, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            String requestBody = "{\"settings\":{\"index\":{\"refresh_interval\":\"-1\",\"blocks\":{\"write\":false}}}}";
            String createIndexResponse = helper.callWebservice(getCurrentBaseURL(), endpointURI, requestBody,
                                                               MimeTypes.MIME_TYPE_NDJSON, -1, null, HttpConstants.METHOD_PUT);
            if (StrUtils.isValid(createIndexResponse)) {
                // Einfacher Check, ob die Anfrage erfolgreich war
                if ((createIndexResponse == null) || !createIndexResponse.contains("\"acknowledged\":true")) {
                    messageHelper.logMsg("Error creating index for " + productId.getProductNumber() + " and language " + language);
                    return OperationResult.OPERATION_FAILED;
                }
            }
        } catch (RESTfulException re) {
            try {
                // Falls es ein Problem gab, liefert der Server ein Error-JSON zurück -> Versuche das JSON zu parsen
                ElasticSearchErrorResponse elasticSearchError = genson.deserialize(re.getStatusText(), ElasticSearchErrorResponse.class);
                messageHelper.logMsg("Error while creating index for " + productId.getProductNumber() + " and language "
                                     + language + "; Status Code: " + elasticSearchError.getStatus() + "; Reason: " + elasticSearchError.getError().getReason());

            } catch (JsonBindingException bindingException) {
                Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                messageHelper.logMsg("Could not parse Exception when creating index " + endpointURI);
                return OperationResult.RESPONSE_NOT_PARSABLE;
            }
            logRestfulException(re);
            return getOperationResultForException(re);
        }
        return OperationResult.OPERATION_SUCCESSFUL;
    }

    /**
     * Führt eine _reindex Operation auf dem Server durch. Dabei werden alle Daten zum übergebenen Produkt vom Quell-Index
     * in den Ziel-Index transferiert
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult doReindex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        String endpointURI = "_reindex?refresh&timeout=120s";
        // Ziel-Index, z.B. parts_de_temp
        String destIndex = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        // Quell-Index der nur Daten zu einem Produkt hält, z.B. parts_de_temp_01f
        String sourceIndex = createProductIndexName(productId, language);
        String requestString = "{\"source\":{\"index\":\"" + sourceIndex + "\",\"query\":{\"match\":{\"prod\":\"" + productId.getProductNumber() + "\"}}},\"dest\":{\"index\":\"" + destIndex + "\"}}";
        try {
            String response = helper.callWebservice(getCurrentBaseURL(), endpointURI, requestString,
                                                    MimeTypes.MIME_TYPE_JSON, -1, null,
                                                    HttpConstants.METHOD_POST);
            if (StrUtils.isValid(response)) {
                ElasticSearchOperationResponse result = genson.deserialize(response, ElasticSearchOperationResponse.class);
                if (StrUtils.isValid(result.getCreated())) {
                    int created = Integer.parseInt(result.getCreated());
                    if (created == 0) {
                        messageHelper.logMsg(created + " entries created for " + productId.getProductNumber() + " and language " + language + "; Response: " + response);
                    } else {
                        messageHelper.logMsg(created + " entries created for " + productId.getProductNumber() + " and language " + language);
                    }
                    return OperationResult.OPERATION_SUCCESSFUL;
                }
            }
        } catch (RESTfulException e) {
            try {
                ElasticSearchErrorResponse elasticSearchError = genson.deserialize(e.getStatusText(), ElasticSearchErrorResponse.class);
                String reason = (elasticSearchError.getError() != null) ? ("; Reason: " + elasticSearchError.getError().getReason()) : "";
                messageHelper.logMsg("Could not reindex index for " + productId.getProductNumber() + " and language "
                                     + language + "; Status Code: " + elasticSearchError.getStatus() + reason
                                     + "; source: " + sourceIndex + ", dest: " + destIndex);
            } catch (JsonBindingException bindingException) {
                Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                messageHelper.logMsg("Could not parse Exception when reindexing data for  " + destIndex + "; Source :" + sourceIndex);
                return OperationResult.RESPONSE_NOT_PARSABLE;
            }
            logRestfulException(e);
            return getOperationResultForException(e);
        }
        messageHelper.logError("Could not reindex data for " + productId.getProductNumber() + " and language " + language);
        return OperationResult.OPERATION_FAILED;
    }

    /**
     * Erzeugt einen Indexnamen für einen Index, der nur die Daten zum übergebenen Produkt hält
     *
     * @param productId
     * @param language
     * @return
     */
    private String createProductIndexName(iPartsProductId productId, String language) {
        String destIndex = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        if (StrUtils.isValid(destIndex)) {
            if ((productId != null) && productId.isValidId()) {
                return destIndex + "_" + productId.getProductNumber().toLowerCase();
            }
        }
        return null;
    }

    /**
     * Löscht den Index zum übergebenen Produkt
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult deleteCompleteProductIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            // URI erzeugen
            String endpointURI = createProductIndexName(productId, language);
            if (StrUtils.isEmpty(endpointURI)) {
                messageHelper.logMsg("Could not create settings endpointURI for language " + language);
                return OperationResult.ENDPOINT_CREATION_FAILED;
            }
            // Anfrage abschicken
            String deleteProductIndexResponse = helper.callWebservice(getCurrentBaseURL(), endpointURI, null,
                                                                      MimeTypes.MIME_TYPE_NDJSON, -1, null, HttpConstants.METHOD_DELETE);
            if (StrUtils.isValid(deleteProductIndexResponse)) {
                // Einfacher Check, ob die Anfrage erfolgreich war
                if ((deleteProductIndexResponse == null) || !deleteProductIndexResponse.contains("\"acknowledged\":true")) {
                    messageHelper.logMsg("Error deleting product index for " + productId.getProductNumber() + " and language " + language);
                    return OperationResult.OPERATION_FAILED;
                }
            }
        } catch (RESTfulException re) {
            try {
                // Falls es ein Problem gab, liefert der Server ein Error-JSON zurück -> Versuche das JSON zu parsen
                ElasticSearchErrorResponse elasticSearchError = genson.deserialize(re.getStatusText(), ElasticSearchErrorResponse.class);
                if (elasticSearchError.indexDoesNotExist()) {
                    return OperationResult.INDEX_DOES_NOT_EXIST;
                } else {
                    messageHelper.logMsg("Error while deleting index for " + productId.getProductNumber() + " and language "
                                         + language + "; Status Code: " + elasticSearchError.getStatus() + "; Reason: "
                                         + elasticSearchError.getError().getReason());
                }
            } catch (JsonBindingException bindingException) {
                Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                messageHelper.logMsg("Could not parse Exception when delete complete product index for " + productId.getProductNumber());
                return OperationResult.RESPONSE_NOT_PARSABLE;
            }
            logRestfulException(re);
            return getOperationResultForException(re);
        }
        return OperationResult.OPERATION_SUCCESSFUL;
    }

    /**
     * Initialisiert die URLs, die in den Admin-Optionen eingetragen sind
     */
    private void initBaseURLs() {
        allBaseURLs = new ArrayList<>();
        String baseURLs = iPartsExportPlugin.getFastSearchBaseURLs();
        if (StrUtils.isValid(baseURLs)) {
            List<String> urls = StrUtils.toStringList(baseURLs, ",", false, true);
            if (!urls.isEmpty()) {
                urls.forEach(url -> {
                    String result = "";
                    if (!url.startsWith("http://")) {
                        result += "http://";
                    }
                    result += url;
                    if (!url.endsWith("/")) {
                        result += "/";
                    }
                    if (StrUtils.isValid(result)) {
                        allBaseURLs.add(result);
                    }
                });
            }
        }
    }

    /**
     * Liefert die URLs zurück, die in der Admin-Option eingetragen sind und mit denen eine Verbindung zustande kam.
     * Optional kann man die maximale Anzahl an gültigen URLs angeben, die zurückgeliefert werden sollen
     *
     * @param limit
     * @return
     */
    private Set<String> getActiveURLs(int limit) {
        Set<String> activeURLs = new LinkedHashSet<>();
        if (Utils.isValid(allBaseURLs)) {
            for (String baseURL : allBaseURLs) {
                if (checkSingleURL(baseURL)) {
                    activeURLs.add(baseURL);
                    if ((limit > 0) && (activeURLs.size() >= limit)) {
                        break;
                    }
                }
            }
        }
        return activeURLs;
    }

    /**
     * Überprüft, ob mit der übergebenen URL eine Verbindung aufgebaut werden kann
     *
     * @param baseURL
     * @return
     */
    private boolean checkSingleURL(String baseURL) {
        try {
            String response = helper.callWebservice(baseURL, "_cat/health?v&pretty", null,
                                                    MimeTypes.MIME_TYPE_JSON, -1, null);
            if (StrUtils.isValid(response)) {
                return true;
            }
        } catch (RESTfulException e) {
            logRestfulException(e);
        }
        return false;
    }

    /**
     * Überprüft, ob man mit den eingestellten Basis-URLs eine Verbindung aufbauen kann. URLs, die gültig sind, werden
     * ausgegeben
     */
    public static void doURLCheckWithMessage() {
        if (StrUtils.isEmpty(iPartsExportPlugin.getFastSearchBaseURLs().trim())) {
            MessageDialog.show("!!In der Admin Konfiguration sind noch keine URLs hinterlegt!");
            return;
        }
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!Cluster-URLs Check", "!!Überprüfe FastSearch Cluster-URLs...", null, true);
        logForm.disableButtons(true);
        Set<String> activeURLs = new LinkedHashSet<>();
        logForm.showModal(thread -> {
            FastSearchConnectionHelper connectionHelper = new FastSearchConnectionHelper();
            Set<String> foundURLs = connectionHelper.getActiveURLs(0);
            if (!foundURLs.isEmpty()) {
                activeURLs.addAll(foundURLs);
            }
        });
        if (activeURLs.isEmpty()) {
            MessageDialog.show("!!Mit keiner der angegebenen URLs konnte eine Verbindung hergestellt werden!");
            return;
        }
        StringBuilder builder = new StringBuilder(TranslationHandler.translate("!!Mit folgenden URLs konnte eine Verbindung aufgebaut werden:"));
        builder.append("\n");
        activeURLs.forEach(url -> {
            builder.append("\n");
            builder.append(url);
        });
        MessageDialog.show(builder.toString());
    }

    /**
     * Initialisiert die Verbindung, sofern die aktuelle Verbindung nicht aktiv ist
     *
     * @return
     */
    public boolean initConnection() {
        if (currentConnectionActive()) {
            return true;
        }
        Set<String> validURL = getActiveURLs(1);

        if (validURL.isEmpty()) {
            return false;
        }
        currentBaseURL = validURL.iterator().next();
        return true;
    }

    /**
     * Check, ob die aktuelle URL eine Verbindung zum Server aufbauen kann
     *
     * @return
     */
    private boolean currentConnectionActive() {
        if (StrUtils.isValid(currentBaseURL)) {
            return checkSingleURL(currentBaseURL);
        }
        return false;
    }

    /**
     * Schickt den übergebenen JSON Inhalt an den Index zur übergebenen Sprache
     *
     * @param language
     * @param jsonContent
     * @param messageHelper
     * @return
     */
    public OperationResult sendJSONForLanguage(String language, String jsonContent, String endpointURI, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            String response = helper.callWebservice(getCurrentBaseURL(), endpointURI,
                                                    jsonContent, MimeTypes.MIME_TYPE_NDJSON, -1,
                                                    null, HttpConstants.METHOD_POST);
            if (StrUtils.isValid(response)) {
                ElasticSearchOperationResponse responseObject = genson.deserialize(response, ElasticSearchOperationResponse.class);
                if (!responseObject.isErrors()) {
                    return OperationResult.OPERATION_SUCCESSFUL;
                } else {
                    messageHelper.logError("Bulk response for " + language + " contains errors! Response: " + response);
                }
            }
        } catch (RESTfulException e) {
            logRestfulException(e, endpointURI);
            return getOperationResultForException(e);
        }
        return OperationResult.OPERATION_FAILED;
    }

    private String createBulkEndpointURI(String language, iPartsProductId productId) {
        String tempIndexName = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        if (StrUtils.isValid(tempIndexName)) {
            if ((productId != null) && productId.isValidId()) {
                tempIndexName += "_" + productId.getProductNumber().toLowerCase();
            }
            return tempIndexName + "/_doc/_bulk";
        }
        return null;
    }

    private String createBulkEndpointURI(String language) {
        return createBulkEndpointURI(language, null);
    }


    private String createWriteSettingEndpointURI(String language) {
        String tempIndexName = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        if (StrUtils.isValid(tempIndexName)) {
            return tempIndexName + "/_settings";
        }
        return null;
    }

    private String createRefreshEndpointURI(String language) {
        String tempIndexName = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        if (StrUtils.isValid(tempIndexName)) {
            return tempIndexName + "/_refresh";
        }
        return null;
    }

    private String createDeleteEndpointURI(String language) {
        String tempIndexName = ElasticSearchIndexMetaData.createIndexName(language, isTestModeActive);
        if (StrUtils.isValid(tempIndexName)) {
            return tempIndexName + "/_delete_by_query?refresh=true";
        }
        return null;
    }

    public List<String> getAllBaseURLs() {
        return allBaseURLs;
    }

    public String getCurrentBaseURL() {
        return currentBaseURL;
    }

    /**
     * Aktualisiert die Indizes für das übergebene Produkt und seine Baumuster
     *
     * @param productId
     * @param langToModelJSON
     * @param messageHelper
     */
    public boolean updateProduct(iPartsProductId productId, Map<String, Map<String, StringBuilder>> langToModelJSON,
                                 MultiThreadExportMessageFileHelper messageHelper) {
        VarParam<Boolean> changeServerConnection = new VarParam<>(false);
        boolean finished = false;
        // Maximal so oft versuchen, wie URLs eingegeben wurden
        for (int i = 0; i < allBaseURLs.size(); i++) {
            finished = sendProductData(productId, langToModelJSON, messageHelper, changeServerConnection);
            // Check, ob die Serververbindung abgebrochen ist
            if (changeServerConnection.getValue()) {
                // Falls ja, zurücksetzen und neue Verbindung aufbauen
                changeServerConnection.setValue(false);
                String oldConnection = currentBaseURL;
                if (initConnection()) {
                    // Es wurde eine gültige Verbindung gefunden -> Produkt im nächsten Loop aktualisieren
                    messageHelper.logMsg("Lost connection to server " + oldConnection + " while sending data for "
                                         + productId.getProductNumber() + ". Established a new connection with "
                                         + currentBaseURL + ". Resending data for " + productId.getProductNumber() + "...");
                } else {
                    // Es wurde keine gültige Verbindung gefunden -> Produkt wird nicht aktualisiert
                    messageHelper.logMsg("Lost connection to server " + oldConnection + " while sending data for "
                                         + productId.getProductNumber() + " and could not establish a new connection!");
                    break;
                }

            } else if (finished) {
                break;
            }
        }

        return finished;
    }

    /**
     * Schickt die Daten des Produkts an den Index-Server
     *
     * @param productId
     * @param langToModelJSON
     * @param messageHelper
     * @param changeServerConnection
     * @return
     */
    private boolean sendProductData(iPartsProductId productId, Map<String, Map<String, StringBuilder>> langToModelJSON,
                                    MultiThreadExportMessageFileHelper messageHelper, VarParam<Boolean> changeServerConnection) {
        ExecutorService executorService = MultiThreadExportExecutor.createExecutor(iPartsExportPlugin.getFastSearchIndexSupplyThreadCount());
        // Pro Sprache soll ein eigener Thread die Anfragen abschicken. Somit arbeitet immer nur ein Thread an einem Index
        boolean useReindex = iPartsExportPlugin.useFastSearchReindexMethod();
        for (Map.Entry<String, Map<String, StringBuilder>> entry : langToModelJSON.entrySet()) {
            String language = entry.getKey();
            executorService.execute(() -> {
                OperationResult result;
                if (useReindex) {
                    result = handleAllModelsForLanguageWithReindex(productId, language, messageHelper, entry.getValue());
                } else {
                    result = handleAllModelsForLanguage(productId, language, messageHelper, entry.getValue());
                }

                if (result == OperationResult.SERVER_UNREACHABLE) {
                    changeServerConnection.setValue(true);
                    executorService.shutdownNow();
                }
            });
        }
        messageHelper.logMsg("Start sending " + productId.getProductNumber() + " data to server...");
        return MultiThreadExportExecutor.executorAwaitTermination(executorService);
    }

    /**
     * Aktualisiert alle Baumuster Daten zu einer Sprache.
     * Ablauf:
     * 1. Schreiboperation auf dem Index erlauben (block.write = false)
     * 2. Falls der Index existiert, alle bisherigen Daten zu den übergebenen Baumuster löschen
     * 3. Pro Baumuster NDJSON eine bulk Anfrage abfeuern
     * 4. Index aktualisieren
     * 5. Schreiboperation auf dem Index blockieren (block.write = true)
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @param builderForOneModel
     * @return
     */
    private OperationResult handleAllModelsForLanguage(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper,
                                                       Map<String, StringBuilder> builderForOneModel) {
        // Schreiboperation auf dem Ziel-Index erlauben (block.write = false) bzw. Anlegen, wenn er nicht existiert
        OperationResult result = checkTargetIndex(productId, language, messageHelper);
        if (operationResultNotValid(result)) {
            // Operation war nicht erfolgreich und der INDEX ist nicht neu -> raus
            return result;
        }

        Set<String> deletedModels = null;
        // Falls der Index existiert, alle bisherigen BM Daten zum Produkt löschen
        if (result != OperationResult.INDEX_DOES_NOT_EXIST) {
            deletedModels = deleteModelDataFromIndex(productId, builderForOneModel, language, messageHelper);
            if (deletedModels == null) {
                // Operation war nicht erfolgreich -> raus
                return OperationResult.SERVER_UNREACHABLE;
            } else if (deletedModels.isEmpty()) {
                return OperationResult.OPERATION_FAILED;
            }
        }

        // Pro Baumuster NDJSON eine bulk Anfrage abfeuern
        String endpointURI = createBulkEndpointURI(language);
        if (StrUtils.isEmpty(endpointURI)) {
            messageHelper.logError("Could not create bulk endpointURI for language " + language);
            return OperationResult.ENDPOINT_CREATION_FAILED;
        }
        result = sendProductBulkData(builderForOneModel, deletedModels, language, endpointURI, null, messageHelper, false);
        if (result == OperationResult.SERVER_UNREACHABLE) {
            // Verbindung zum Server ist bei einer Abfrage abgebrochen -> raus und Produkt nochmals versorgen
            return result;
        }

        // Index aktualisieren
        endpointURI = createRefreshEndpointURI(language);
        if (operationResultNotValid(refreshIndex(language, endpointURI, messageHelper))) {
            // Aktualisieren war nicht erfolgreich -> Fehlermeldung und danach versuchen das Schreiben zu blockieren
            messageHelper.logMsg("Error while refreshing data for language " + language);
        }

        // Schreiboperation auf dem Index blockieren (block.write = true)
        return changeIndexWriteOperation(productId, true, language, messageHelper);
    }

    /**
     * Sendet die BM JSONs als _bulk Anfrage an den Server. Optional kann via <code>validModels</code> eingegrenzt werden,
     * welche BM verschickt werden sollen
     *
     * @param builderForOneModel
     * @param validModels
     * @param language
     * @param endpointURI
     * @param refreshEndpoint
     * @param messageHelper
     * @return
     */
    private OperationResult sendProductBulkData(Map<String, StringBuilder> builderForOneModel, Set<String> validModels,
                                                String language, String endpointURI, String refreshEndpoint,
                                                MultiThreadExportMessageFileHelper messageHelper, boolean cancelIfOneRequestInvalid) {
        OperationResult result;
        int modelJSONCount = 0;
        int reindexRefreshRate = iPartsExportPlugin.getFastSearchReindexRefreshRate();
        for (Map.Entry<String, StringBuilder> modelToJSONs : builderForOneModel.entrySet()) {
            // Check, ob das BM verarbeitet werden soll
            String modelNumber = modelToJSONs.getKey();
            if ((validModels != null) && !validModels.contains(modelNumber)) {
                messageHelper.logMsg("Data for model " + modelNumber + " will not be send because data deletion failed!");
                continue;
            }
            StringBuilder builder = modelToJSONs.getValue();
            if (builder != null) {
                result = sendJSONForLanguage(language, builder.toString(), endpointURI, messageHelper);
                modelJSONCount++;
                if (operationResultNotValid(result)) {
                    messageHelper.logMsg("Error while sending data for model " + modelNumber);
                    // Check, ob man bei einem ungültigen Request abbrechen soll
                    if (cancelIfOneRequestInvalid) {
                        return result;
                    }
                }
                if (result == OperationResult.SERVER_UNREACHABLE) {
                    // Verbindung zum Server ist bei einer Abfrage abgebrochen -> raus und Produkt nochmals versorgen
                    return result;
                }
                if ((reindexRefreshRate > 0) && StrUtils.isValid(refreshEndpoint) && iPartsExportPlugin.useFastSearchReindexMethod()) {
                    if ((modelJSONCount % reindexRefreshRate) == 0) {
                        result = refreshIndex(language, refreshEndpoint, messageHelper);
                        if (result == OperationResult.SERVER_UNREACHABLE) {
                            // Verbindung zum Server ist bei einer Abfrage abgebrochen -> raus und Produkt nochmals versorgen
                            return result;
                        }
                    }
                }
            }
        }
        return OperationResult.OPERATION_SUCCESSFUL;
    }

    /**
     * Überprüft, ob das übergebenen {@link OperationResult} gültig ist. Gültig ist eine Operation nur, wenn sie
     * erfolgreich ist oder wenn es den Index noch nicht gibt. Optional werden für einzelne Operationen generische
     * Meldungen ausgegeben
     *
     * @param operation
     * @return
     */
    private boolean operationResultNotValid(OperationResult operation) {
        boolean result = true;
        switch (operation) {
            case RESPONSE_NOT_PARSABLE:
            case ENDPOINT_CREATION_FAILED:
            case OPERATION_FAILED:
            case OPERATION_FAILED_WITH_EXCEPTION:
            case SERVER_UNREACHABLE:
                result = false;
                break;
        }
        return !result;
    }

    /**
     * Sendet den Befehl, um Schreiboperationen auf dem Index zu erlauben
     *
     * @param productId
     * @param blockWriteOperation
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult changeIndexWriteOperation(iPartsProductId productId, boolean blockWriteOperation,
                                                      String language, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            // URI erzeugen
            String endpointURI = createWriteSettingEndpointURI(language);
            if (StrUtils.isEmpty(endpointURI)) {
                messageHelper.logMsg("Could not create settings endpointURI for language " + language);
                return OperationResult.ENDPOINT_CREATION_FAILED;
            }
            String refreshRate;
            if (blockWriteOperation) {
                refreshRate = "60s";
            } else {
                refreshRate = "-1";
            }
            // Request Body erzeugen
            String requestString = "{\"settings\":{\"index.blocks.write\":" + blockWriteOperation + ",\"index.refresh_interval\":\"" + refreshRate + "\"}}";
            // Anfrage abschicken
            String changeIndexSettingResponse = helper.callWebservice(getCurrentBaseURL(), endpointURI, requestString,
                                                                      MimeTypes.MIME_TYPE_NDJSON, -1, null, HttpConstants.METHOD_PUT);
            if (StrUtils.isValid(changeIndexSettingResponse)) {
                // Einfacher Check, ob die Anfrage erfolgreich war
                if ((changeIndexSettingResponse == null) || !changeIndexSettingResponse.contains("\"acknowledged\":true")) {
                    messageHelper.logMsg("Could not change writable setting to false for index " + productId.getProductNumber() + " and language " + language);
                    return OperationResult.OPERATION_FAILED;
                }
            }
        } catch (RESTfulException re) {
            try {
                // Falls es ein Problem gab, liefert der Server ein Error-JSON zurück -> Versuche das JSON zu parsen
                ElasticSearchErrorResponse elasticSearchError = genson.deserialize(re.getStatusText(), ElasticSearchErrorResponse.class);
                // Existiert der Index nicht, kann der Index trotzdem aufgebaut werden
                if (elasticSearchError.indexDoesNotExist()) {
                    return OperationResult.INDEX_DOES_NOT_EXIST;
                } else {
                    messageHelper.logMsg("Error while changing index settings for " + productId.getProductNumber() + " and language "
                                         + language + "; Status Code: " + elasticSearchError.getStatus() + "; Reason: "
                                         + elasticSearchError.getError().getReason());
                }
            } catch (JsonBindingException bindingException) {
                Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                messageHelper.logMsg("Could not parse Exception when changing settings for product " + productId.getProductNumber() + "; Block write operation: " + blockWriteOperation);
                return OperationResult.RESPONSE_NOT_PARSABLE;
            }
            logRestfulException(re);
            return getOperationResultForException(re);
        }
        return OperationResult.OPERATION_SUCCESSFUL;
    }

    /**
     * Liefert das {@link OperationResult} zur erhaltenen {@link RESTfulException}
     *
     * @param re
     * @return
     */
    private OperationResult getOperationResultForException(RESTfulException re) {
        if ((re.getHttpStatus() == HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR) && re.getStatusText().contains("Connection timed out")) {
            return OperationResult.SERVER_UNREACHABLE;
        } else {
            return OperationResult.OPERATION_FAILED_WITH_EXCEPTION;
        }
    }

    /**
     * Aktuaisiert den Index
     *
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult refreshIndex(String language, String endpointURI, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            if (StrUtils.isEmpty(endpointURI)) {
                messageHelper.logError("Could not create refresh endpointURI for language " + language);
                return OperationResult.ENDPOINT_CREATION_FAILED;
            }
            String response = helper.callWebservice(getCurrentBaseURL(), endpointURI, null,
                                                    MimeTypes.MIME_TYPE_NDJSON, -1, null);
            if (StrUtils.isValid(response)) {
                return OperationResult.OPERATION_SUCCESSFUL;
            }
        } catch (RESTfulException e) {
            logRestfulException(e);
            return getOperationResultForException(e);
        }
        return OperationResult.OPERATION_FAILED;
    }

    /**
     * Löscht alle Daten zu einem Produkt aus dem Index zur übergebenen Sprache
     *
     * @param productId
     * @param language
     * @param messageHelper
     * @return
     */
    private OperationResult deleteProductFromIndex(iPartsProductId productId, String language, MultiThreadExportMessageFileHelper messageHelper) {
        try {
            String endpointURI = createDeleteEndpointURI(language);
            if (StrUtils.isEmpty(endpointURI)) {
                messageHelper.logError("Could not create delete endpointURI for language " + language);
                return OperationResult.ENDPOINT_CREATION_FAILED;
            }
            String requestString = "{\"query\": {\"match\": {\"prod\": \"" + productId.getProductNumber() + "\"}}}";
            String response = helper.callWebservice(getCurrentBaseURL(), endpointURI, requestString,
                                                    MimeTypes.MIME_TYPE_NDJSON, -1, null, HttpConstants.METHOD_POST);
            if (StrUtils.isValid(response)) {
                ElasticSearchOperationResponse result = genson.deserialize(response, ElasticSearchOperationResponse.class);
                if (StrUtils.isValid(result.getDeleted())) {
                    int deleted = Integer.parseInt(result.getDeleted());
                    if (deleted > 0) {
                        messageHelper.logMsg(deleted + " entries deleted for " + productId.getProductNumber() + " and language " + language);
                    }
                    return OperationResult.OPERATION_SUCCESSFUL;
                }
            }
        } catch (RESTfulException e) {
            try {
                ElasticSearchErrorResponse elasticSearchError = genson.deserialize(e.getStatusText(), ElasticSearchErrorResponse.class);
                String reason = (elasticSearchError.getError() != null) ? ("; Reason: " + elasticSearchError.getError().getReason()) : "";
                messageHelper.logMsg("Could not delete index for " + productId.getProductNumber() + " and language "
                                     + language + "; Status Code: " + elasticSearchError.getStatus() + reason);
            } catch (JsonBindingException bindingException) {
                Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                messageHelper.logMsg("Could not parse Exception when deleting product data for " + productId.getProductNumber());
                return OperationResult.RESPONSE_NOT_PARSABLE;
            }
            logRestfulException(e);
            return getOperationResultForException(e);
        }
        messageHelper.logError("Could not delete data for " + productId.getProductNumber() + " and language " + language);
        return OperationResult.OPERATION_FAILED;
    }

    /**
     * Löscht die Daten zu einer Baumuster-Produktbeziehung
     *
     * @param productId
     * @param builderForOneModel
     * @param language
     * @param messageHelper
     * @return
     */
    private Set<String> deleteModelDataFromIndex(iPartsProductId productId, Map<String, StringBuilder> builderForOneModel,
                                                 String language, MultiThreadExportMessageFileHelper messageHelper) {
        Set<String> validModels = new HashSet<>();
        String endpointURI = createDeleteEndpointURI(language);
        if (StrUtils.isEmpty(endpointURI)) {
            messageHelper.logError("Could not create delete endpointURI for language " + language);
            return validModels;
        }
        int dataDeleted = 0;
        for (String model : builderForOneModel.keySet()) {
            String modeIdForFastSearch = FastSearchTextHelper.getFastSearchModelId(model, productId.getProductNumber());
            if (StrUtils.isEmpty(modeIdForFastSearch)) {
                continue;
            }
            try {
                String requestString = "{\"query\": {\"match\": {\"modelid\": \"" + modeIdForFastSearch + "\"}}}";
                String response = helper.callWebservice(getCurrentBaseURL(), endpointURI, requestString,
                                                        MimeTypes.MIME_TYPE_NDJSON, -1, null, HttpConstants.METHOD_POST);
                if (StrUtils.isValid(response)) {
                    ElasticSearchOperationResponse result = genson.deserialize(response, ElasticSearchOperationResponse.class);
                    if (StrUtils.isValid(result.getDeleted())) {
                        int deleted = Integer.parseInt(result.getDeleted());
                        if (deleted > 0) {
                            dataDeleted += deleted;
                        }
                        validModels.add(model);
                        continue;
                    }
                }
            } catch (RESTfulException e) {
                try {
                    ElasticSearchErrorResponse elasticSearchError = genson.deserialize(e.getStatusText(), ElasticSearchErrorResponse.class);
                    String reason = (elasticSearchError.getError() != null) ? ("; Reason: " + elasticSearchError.getError().getReason()) : "";
                    messageHelper.logMsg("Could not delete data for " + productId.getProductNumber() + ", modelid "
                                         + modeIdForFastSearch + " and language " + language + "; Status Code: "
                                         + elasticSearchError.getStatus() + reason);
                } catch (JsonBindingException bindingException) {
                    Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, bindingException);
                    messageHelper.logMsg("Could not parse Exception when deleting model data for " + productId.getProductNumber()
                                         + ", modelid " + modeIdForFastSearch + " and language " + language);
                    continue;
                }
                logRestfulException(e);
                OperationResult operationResult = getOperationResultForException(e);
                if (operationResult == OperationResult.SERVER_UNREACHABLE) {
                    return null;
                }
                continue;
            }
            messageHelper.logError("Could not delete data for " + productId.getProductNumber() + ", modelid "
                                   + modeIdForFastSearch + " and language " + language);
            continue;
        }
        if (dataDeleted > 0) {
            messageHelper.logMsg(dataDeleted + " entries deleted for " + productId.getProductNumber() + ", language " + language + " and models " + String.join(", ", validModels));
        }
        return validModels;
    }


    private void logRestfulException(RESTfulException e, String additionalInfo) {
        Logger.log(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, LogType.DEBUG, e.getHttpStatus() + " - "
                                                                              + e.getStatusText()
                                                                              + (StrUtils.isValid(e.getMessage()) ? "\n" + e.getMessage() : "")
                                                                              + (StrUtils.isValid(additionalInfo) ? "Additional info: " + additionalInfo : ""));
    }

    private void logRestfulException(RESTfulException e) {
        logRestfulException(e, null);
    }
}
