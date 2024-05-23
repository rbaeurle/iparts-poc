/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.AuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.ChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.SupplyAuthorOrderToBST;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.*;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für die Versorgung eines Autoren-Auftrags an BST.
 */
public class SupplyAuthorOrderToBSTHelper {

    private EtkProject project;
    private CallRESTFulWebserviceHelper callRESTFulWebserviceHelper = new CallRESTFulWebserviceHelper(true);

    public SupplyAuthorOrderToBSTHelper(EtkProject project) {
        this.project = project;
    }

    /**
     * Versorgt den übergebenen Autoren-Auftrag an BST.
     *
     * @param dataAuthorOrder
     * @return
     */
    public boolean supplyAuthorOrderToBST(iPartsDataAuthorOrder dataAuthorOrder) {
        String messageDialogTitle = "!!Autoren-Auftrag an BST versorgen";
        String authorOrderName = dataAuthorOrder.getAuthorOrderName();

        // URI für BST-Webservice muss konfiguriert sein
        String bstURI = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_URI_SUPPLY_AUTHOR_ORDERS_TO_BST).trim();
        if (bstURI.isEmpty()) {
            MessageDialog.showError(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil keine URI für den Aufruf vom BST-Webservice konfiguriert ist.",
                                                                 authorOrderName), messageDialogTitle);
            return false;
        }

        // Für die Versorgung an BST muss der Autoren-Auftrag freigegeben sein und einen zugewiesenen Bearbeitungsauftrag haben
        if (!iPartsAuthorOrderStatus.isEndState(dataAuthorOrder.getStatus())) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil er noch nicht freigegeben ist.",
                                                                   authorOrderName), messageDialogTitle);
            return false;
        }
        String bstId = dataAuthorOrder.getBstId();
        if (bstId.isEmpty()) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil kein Bearbeitungsauftrag zugewiesen ist.",
                                                                   authorOrderName), messageDialogTitle);
            return false;
        }

        // Existiert der Bearbeitungsauftrag in der DB?
        iPartsDataWorkOrder dataWorkOrder = new iPartsDataWorkOrder(project, new iPartsWorkOrderId(bstId));
        if (!dataWorkOrder.existsInDB()) {
            MessageDialog.showError(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil der Bearbeitungsauftrag mit der BST-ID \"%2\" nicht gefunden wurde.",
                                                                 authorOrderName, bstId), messageDialogTitle);
            return false;
        }

        // ChangeSetEntries laden -> dürfen nicht leer sein
        iPartsChangeSetId changeSetId = dataAuthorOrder.getChangeSetId();
        iPartsDataChangeSetEntryList dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(project,
                                                                                                                                                   changeSetId,
                                                                                                                                                   null);
        if (dataChangeSetEntryList.isEmpty()) {
            MessageDialog.showError(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil der Autoren-Auftrag keine Änderungen enthält.",
                                                                 authorOrderName), messageDialogTitle);
            return false;
        }

        // JSON zusammenbauen
        SupplyAuthorOrderToBST supplyToBST = new SupplyAuthorOrderToBST(dataWorkOrder);
        supplyToBST.setAuthorOrder(new AuthorOrder(dataAuthorOrder));

        if (StrUtils.isEmpty(supplyToBST.getAuthorOrder().getReleaseDate())) {
            MessageDialog.showError(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" kann nicht an BST versorgt werden, weil kein Freigabedatum ermittelt werden konnte.",
                                                                 authorOrderName), messageDialogTitle);
            return false;
        }

        // ChangeSetEntries hinzufügen
        List<ChangeSetEntry> changeSetEntries = new DwList<>();
        Map<String, ChangeSetEntry> modulePicOrderMap = new HashMap<>();
        List<ChangeSetEntry> validPicOrderList = new DwList<>();
        for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntryList) {
            ChangeSetEntry changeSetEntry = new ChangeSetEntry(dataChangeSetEntry);
            if (changeSetEntry.getMergedData() != null) { // ChangeSetEntries ohne Inhalt sind irrelevant
                if (collectInvalidPicOrderEntries(changeSetEntry, modulePicOrderMap, validPicOrderList)) {
                    continue;
                }
                changeSetEntries.add(changeSetEntry);
            }
        }

        addValidPicOrderModules(modulePicOrderMap, validPicOrderList, changeSetEntries);
        supplyToBST.setChangeSetEntries(changeSetEntries);

        // JSON validieren
        try {
            supplyToBST.checkIfValid(null);
        } catch (RESTfulWebApplicationException e) {
            String errorText = String.valueOf(e.getHttpStatus());
            RESTfulErrorResponse response = e.getResponse();
            if (response != null) {
                Object responseObject = response.getResponseObject();
                if (responseObject != null) {
                    if (responseObject instanceof WSErrorResponse) {
                        WSErrorResponse errorResponse = (WSErrorResponse)responseObject;
                        errorText = "error code " + errorResponse.getCode() + " - " + errorResponse.getMessage();
                    } else {
                        errorText = responseObject.toString();
                    }
                }
            }
            RuntimeException validationException = new RuntimeException("JSON validation check failed while supplying author order \""
                                                                        + authorOrderName + "\" to BST: " + errorText);
            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.ERROR, validationException);
            MessageDialog.showError(TranslationHandler.translate("!!Fehler bei der Validierung der Versorgung vom Autoren-Auftrag \"%1\" an BST.",
                                                                 authorOrderName), messageDialogTitle);
            return false;
        }

        // Timeout
        int timeout = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TIMEOUT);

        // Config auslesen, und ggf. Token als Request Header mitschicken
        Map<String, String> requestProperties = null;
        String token = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN).trim();
        if (StrUtils.isValid(token)) { // Nur wenn ein Token gesetzt ist die weiteren Werte aus der Config auslesen
            String headerName = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_NAME);
            String tokenType = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_TYPE);
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.DEBUG, "Using token for BST webservice call: "
                                                                                  + headerName + "=" + tokenType + " "
                                                                                  + token);
            requestProperties = new HashMap<>();
            requestProperties.put(headerName, tokenType + " " + token);
        }

        try {
            String response = callRESTFulWebserviceHelper.callWebservice(bstURI, "", supplyToBST, null, timeout, requestProperties);
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.DEBUG, "Response from BST webservice " + bstURI
                                                                                  + " for supplying author order \""
                                                                                  + dataAuthorOrder.getAsId().getAuthorGuid() + "\": "
                                                                                  + HttpConstants.HTTP_STATUS_OK + " - "
                                                                                  + response);

            // Flag für erfolgreiche Versorgung setzen, evtl. Fehler-Text entfernen und speichern
            dataAuthorOrder.setFieldValueAsBoolean(iPartsConst.FIELD_DAO_BST_SUPPLIED, true, DBActionOrigin.FROM_EDIT);
            dataAuthorOrder.setFieldValue(iPartsConst.FIELD_DAO_BST_ERROR, "", DBActionOrigin.FROM_EDIT);
            dataAuthorOrder.saveToDB();

            return true;
        } catch (RESTfulException e) {
            String bstError;
            String message;
            if (e.getHttpStatus() == HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR) {
                RuntimeException webserviceException = new RuntimeException("Error while calling BST webservice " + bstURI
                                                                            + " for supplying author order \""
                                                                            + dataAuthorOrder.getAsId().getAuthorGuid()
                                                                            + "\": " + e.getStatusText(), e);
                Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.ERROR, webserviceException);

                bstError = "Error " + e.getStatusText() + "\n" + Utils.exceptionToString(e);
                message = TranslationHandler.translate("!!Fehler beim Aufruf vom BST Webservice für den Autoren-Auftrag \"%1\".",
                                                       authorOrderName);
            } else {
                int httpResponseCode = e.getHttpStatus();
                String httpResponseMessage = e.getStatusText();
                if ((httpResponseCode == HttpConstants.HTTP_STATUS_FORBIDDEN) || (httpResponseCode == HttpConstants.HTTP_STATUS_NOT_FOUND)) {
                    message = TranslationHandler.translate("!!BST Webservice nicht erreichbar für den Autoren-Auftrag \"%1\".",
                                                           authorOrderName);
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                    message = TranslationHandler.translate("!!Authorisierungsfehler beim Aufruf vom BST Webservice für den Autoren-Auftrag \"%1\".",
                                                           authorOrderName);
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT) {
                    message = TranslationHandler.translate("!!Timeout beim Aufruf vom BST Webservice für den Autoren-Auftrag \"%1\".",
                                                           authorOrderName);
                } else {
                    message = TranslationHandler.translate("!!Fehlerantwort vom BST Webservice für den Autoren-Auftrag \"%1\": %2",
                                                           authorOrderName, httpResponseCode + " - " + httpResponseMessage);
                }

                String errorResponse = httpResponseCode + " - " + httpResponseMessage;
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.ERROR, "Error response from BST webservice " + bstURI
                                                                                      + " for supplying author order \""
                                                                                      + dataAuthorOrder.getAsId().getAuthorGuid()
                                                                                      + "\": " + errorResponse);

                bstError = "HTTP status " + errorResponse;
            }

            // Fehler-Text speichern
            dataAuthorOrder.setFieldValue(iPartsConst.FIELD_DAO_BST_ERROR, bstError, DBActionOrigin.FROM_EDIT);
            dataAuthorOrder.saveToDB();

            MessageDialog.showError(message, messageDialogTitle);
            return false;
        }
    }

    /**
     * Filtert die ungültigen Bildauftrag zu Modul Verknüpfungen aus und fügt nur die gültigen dazu.
     *
     * @param modulePicOrderMap
     * @param validPicOrderList
     * @param changeSetEntries
     */
    private void addValidPicOrderModules(Map<String, ChangeSetEntry> modulePicOrderMap, List<ChangeSetEntry> validPicOrderList,
                                         List<ChangeSetEntry> changeSetEntries) {
        if (validPicOrderList.isEmpty() || modulePicOrderMap.isEmpty()) {
            return;
        }
        // Die Liste enthält alle gültigen Bildaufträge. Über die Bildauftrags-ID werden die Verknüpfungen gefiltert.
        for (ChangeSetEntry picOrderEntry : validPicOrderList) {
            iPartsPicOrderId picOrderId = new iPartsPicOrderId(picOrderEntry.getMergedData().getPkValues());
            changeSetEntries.add(modulePicOrderMap.get(picOrderId.getOrderGuid()));
        }
    }

    /**
     * Sammelt alle ungültigen Bildaufträge und alle Bildauftrag zu Modul Verknüpfungen, damit später die ungültigen
     * Verknüpfungen und Bildaufträge nicht an BST geschickt werden.
     *
     * @param changeSetEntry
     * @param modulePicOrderMap
     * @param validPicOrdersList
     * @return
     */
    private boolean collectInvalidPicOrderEntries(ChangeSetEntry changeSetEntry, Map<String, ChangeSetEntry> modulePicOrderMap,
                                                  List<ChangeSetEntry> validPicOrdersList) {
        if (changeSetEntry.getMergedData() != null) {
            SerializedDBDataObject serializedObject = changeSetEntry.getMergedData();
            if (changeSetEntry.getDataObjectType().equals(iPartsPicOrderModulesId.TYPE)) {
                iPartsPicOrderModulesId modulesId = new iPartsPicOrderModulesId(serializedObject.getPkValues());
                modulePicOrderMap.put(modulesId.getOrderGuid(), changeSetEntry);
                return true;
            }
            if (changeSetEntry.getDataObjectType().equals(iPartsPicOrderId.TYPE)) {
                String invalidAttribute = serializedObject.getAttributeValue(iPartsConst.FIELD_DA_PO_ORDER_INVALID, false, project);
                // Wenn das "ungültig" Attribut nicht vorkommt, dann ist der Bildauftrag gültig
                if (invalidAttribute != null) {
                    boolean isInvalid = SQLStringConvert.ppStringToBoolean(invalidAttribute);
                    if (isInvalid) {
                        return true;
                    }
                }
                validPicOrdersList.add(changeSetEntry);
            }
        }
        return false;
    }
}