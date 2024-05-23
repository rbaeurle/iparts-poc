/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.bst;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointBST;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.Supplier;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.Task;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

/**
 * Endpoint für den BST-Webservice
 */
public class iPartsWSBSTEndpoint extends iPartsWSAbstractEndpointBST<iPartsWSBSTRequest> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/iPartsEdit/bst";

    public iPartsWSBSTEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(0); // Der BST Webservice hat keinen JSON-Response-Cache
    }

    @Override
    protected iPartsWSBSTResponse executeWebservice(EtkProject project, iPartsWSBSTRequest requestObject) {

        // Speichern erfolgt in einer Transaktion, damit nicht zwei Anfragen parallel das gleiche Datenobjekt speichern können.
        project.getDbLayer().startTransaction();
        try {
            // WorkOrder Datenobjekt mit den Daten aus dem Requestobject befüllen
            iPartsWorkOrderId workOrderId = new iPartsWorkOrderId(requestObject.getWpid());
            iPartsDataWorkOrder workOrder = new iPartsDataWorkOrder(project, workOrderId);
            if (!workOrder.existsInDB()) {
                workOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Bestellnummer
                setFieldValueFromRequest(requestObject.getOrderNo(), workOrder, FIELD_DWO_ORDER_NO);
                // Baureihen Fahrzeug und Aggregate
                setFieldValueAsArrayFromRequest(requestObject.getSeries(), workOrder, FIELD_DWO_SERIES);
                // Sparte (Branch)
                setFieldValueFromRequest(requestObject.getBranch(), workOrder, FIELD_DWO_BRANCH);
                // Untersparten
                setFieldValueAsArrayFromRequest(requestObject.getSubBranches(), workOrder, FIELD_DWO_SUB_BRANCHES);
                // Kennzeichen "kostenneutraler Bearbeitungsauftrag"
                workOrder.setFieldValueAsBoolean(FIELD_DWO_COST_NEUTRAL, requestObject.getIsCostNeutral(), DBActionOrigin.FROM_EDIT);
                // Kennzeichen "interner Bearbeitungsauftrag"
                workOrder.setFieldValueAsBoolean(FIELD_DWO_INTERNAL_ORDER, requestObject.getIsInternalOrder(), DBActionOrigin.FROM_EDIT);
                // Abrufnummer
                setFieldValueFromRequest(requestObject.getReleaseNo(), workOrder, FIELD_DWO_RELEASE_NO);
                // Titel
                setFieldValueFromRequest(requestObject.getTitle(), workOrder, FIELD_DWO_TITLE);
                // Geplantes Lieferdatum
                setFieldValueAsDateFromRequest(requestObject.getWorkDeliveryTs(), workOrder, FIELD_DWO_DELIVERY_DATE_PLANNED);
                // Leistungsbeginn
                setFieldValueAsDateFromRequest(requestObject.getWorkBeginTs(), workOrder, FIELD_DWO_START_OF_WORK);
                Supplier supplier = requestObject.getSupplier();
                if (!requestObject.getIsInternalOrder() && (supplier != null)) {
                    // Kein interner Auftrag --> Es muss einen Supplier geben. Wurde in Validitätsprüfung auch sichergestellt.
                    // Lieferantennummer
                    setFieldValueFromRequest(supplier.getId(), workOrder, FIELD_DWO_SUPPLIER_NO);
                    // Kurzbezeichnung Lieferante
                    setFieldValueFromRequest(supplier.getShortname(), workOrder, FIELD_DWO_SUPPLIER_SHORTNAME);
                    // Name Lieferant
                    setFieldValueFromRequest(supplier.getName(), workOrder, FIELD_DWO_SUPPLIER_NAME);
                }
            } else {
                // Sollte die wpid bereits in der Datenbank exisitieren wird ein Fehlercode zurückgegeben und die Verarbeitung abgebrochen.
                throwError(HttpConstants.HTTP_STATUS_CONFLICT, WSError.REQUEST_CONFLICT_EXISTS, "workorder (" + workOrderId.getBSTId()
                                                                                                + ") already exists in database. Aborting import.", null);
            }

            // WorkOrderTask Datenobjekte mit den Daten aus dem Requestobject befüllen
            iPartsDataWorkOrderTaskList workOrderTaskList = workOrder.getTasks();
            if (!workOrderTaskList.isEmpty()) {
                // Kann eigentlich nicht passieren, da wenn wir hier ankommen ein neues WorkOrder Datenobjekt mit neuer DWO_BST_ID
                // angelegt wurde, also haben alle WorkOrderTasks auch diese neue DWT_BST_ID.
                throwError(HttpConstants.HTTP_STATUS_CONFLICT, WSError.REQUEST_CONFLICT_EXISTS, "workorder tasks already exist for new workorder ("
                                                                                                + workOrderId.getBSTId() + "). Aborting import.", null);
            }
            int lfdNr = 1;
            for (Task task : requestObject.getTasks()) {
                iPartsWorkOrderTaskId workOrderTaskId = new iPartsWorkOrderTaskId(requestObject.getWpid(), EtkDbsHelper.formatLfdNr(lfdNr));
                iPartsDataWorkOrderTask workOrderTask = new iPartsDataWorkOrderTask(project, workOrderTaskId);

                // Datenobject existiert noch nicht, das wurde oben schon (auch innerhalb der Transaktion) abgeprüft.
                workOrderTask.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Name - Leistungsart der Position
                setFieldValueFromRequest(task.getActivityName(), workOrderTask, FIELD_DWT_ACTIVITY_NAME);
                // Leistungsart der Position
                setFieldValueFromRequest(task.getActivityType(), workOrderTask, FIELD_DWT_ACTIVITY_TYPE);
                workOrderTask.setFieldValueAsInteger(FIELD_DWT_AMOUNT, task.getAmount(), DBActionOrigin.FROM_EDIT);

                workOrderTaskList.add(workOrderTask, DBActionOrigin.FROM_EDIT);
                lfdNr++;
            }

            workOrder.saveToDB();
            project.getDbLayer().commit();

            // War die Speicherung erfolgreich wird Status 200 mit leerem Json zurückgegeben.
            return new iPartsWSBSTResponse();

        } catch (RESTfulWebApplicationException wae) {
            project.getDbLayer().rollback();
            throw wae;
        } catch (Exception e) {
            project.getDbLayer().rollback();
            throwProcessingError(WSError.INTERNAL_ERROR, e.getMessage(), null);
            return null;
        }

    }

    /**
     * Überträgt den übergebenen Request-Wert als Array auf das übergebene EtkDataObject
     *
     * @param requestDateValue
     * @param workOrder
     * @param dbObjectFieldName
     */
    private void setFieldValueAsDateFromRequest(String requestDateValue, iPartsDataWorkOrder workOrder, String dbObjectFieldName) throws ParseException, DateException {
        if (StrUtils.isValid(requestDateValue)) {
            Calendar calendarValue = DateUtils.toCalendar_ISO(requestDateValue);
            workOrder.setFieldValueAsDate(dbObjectFieldName, calendarValue, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Überträgt den übergebenen Request-Wert als Array auf das übergebene EtkDataObject
     *
     * @param requestArrayValues
     * @param workOrder
     * @param dbObjectFieldName
     */
    private void setFieldValueAsArrayFromRequest(List<String> requestArrayValues, iPartsDataWorkOrder workOrder, String dbObjectFieldName) {
        if (requestArrayValues != null) {
            EtkDataArray seriesArray = new EtkDataArray();
            seriesArray.add(requestArrayValues);
            workOrder.setFieldValueAsArray(dbObjectFieldName, seriesArray, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Überträgt den übergebenen Request-Wert auf das übergebene EtkDataObject
     *
     * @param requestValue
     * @param workOrder
     * @param dbObjectFieldName
     */
    private void setFieldValueFromRequest(String requestValue, EtkDataObject workOrder, String dbObjectFieldName) {
        if (StrUtils.isValid(requestValue)) {
            workOrder.setFieldValue(dbObjectFieldName, requestValue, DBActionOrigin.FROM_EDIT);
        }
    }
}
