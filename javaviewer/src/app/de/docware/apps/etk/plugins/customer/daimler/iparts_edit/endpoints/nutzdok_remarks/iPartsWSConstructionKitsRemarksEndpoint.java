/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsNutzDokRemarkId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportMethod;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointNutzDok;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketRemarkItem;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Consumes;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.POST;
import de.docware.framework.modules.webservice.restful.annotations.methods.PUT;

/**
 * Webservice für das Anlegen von Bemerkungen für Arbeitsaufträge aus NutzDok, für KEMs oder SAAs in der Tabelle
 * {@link iPartsConst#TABLE_DA_NUTZDOK_REMARK}.
 */
public class iPartsWSConstructionKitsRemarksEndpoint extends iPartsWSAbstractEndpointNutzDok<iPartsWSWorkBasketRemarkItem> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/iPartsEdit/constructionKits/annotation";

    public iPartsWSConstructionKitsRemarksEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(0); // Der ConstructionKitsRemarks Webservice hat keinen JSON-Response-Cache
    }

    /**
     * DAIMLER-9734: PUT-Aufruf soll auch unterstützt werden und sich genau wir der POST-Aufruf verhalten.
     *
     * @return
     */
    @PUT
    @POST
    @Produces(MimeTypes.MIME_TYPE_JSON)
    @Consumes(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface handleWebserviceRequest(iPartsWSWorkBasketRemarkItem requestObject) {
        return handleWebserviceRequestIntern(requestObject);
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSWorkBasketRemarkItem requestObject) throws RESTfulWebApplicationException {

        ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("Import constructionKits annotation WebService");

        iPartsCortexImportEndpointNames endPointName = determineEndpointNameByRequestType(logHelper, requestObject.getTypeAsEnum());
        if (endPointName != iPartsCortexImportEndpointNames.UNKNOWN) {
            CortexResult cortexResult = createAndSaveCortexElement(project, logHelper, requestObject, endPointName,
                                                                   iPartsCortexImportMethod.INSERT);
            if (cortexResult == CortexResult.OK_STOP) {
                // keine weitere WebService-Endpoint Logik
                iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
            } else if (cortexResult == CortexResult.ERROR) {
                // es ist ein Fehler aufgetreten beim Speichern des CortexRecordsImports
                iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
            } else {
                // der ehemalige WebService Endpoint
                try {
                    GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
                    iPartsDataNutzDokRemark nutzDokRemark = handleWorkBasketRemarkItem(project, requestObject, logHelper);
                    String finalMessage;
                    if (nutzDokRemark != null) {
                        dataObjectsToBeSaved.add(nutzDokRemark, DBActionOrigin.FROM_EDIT);
                        saveInTransaction(project, dataObjectsToBeSaved);
                        finalMessage = "!!Import erfolgreich. Es wurde eine Bemerkung importiert.";
                    } else {
                        finalMessage = "!!Import erfolgreich. Es wurde keine neue oder veränderte Bemerkung importiert.";
                    }

                    logHelper.fireLineSeparator();
                    logHelper.addLogMsgWithTranslation(finalMessage);

                    iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
                } catch (RuntimeException e) {
                    // Falls ein Fehler auftritt, diesen abfangen und weiterwerfen, damit man das Job-Log hier abbrechen kann.
                    fireExceptionLogErrors(logHelper, null, e);
                    iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
                    throw e;
                }
            }
        }
        return null;
    }

    private iPartsCortexImportEndpointNames determineEndpointNameByRequestType(ImportExportLogHelper logHelper, iPartsWSWorkBasketItem.TYPE type) {
        iPartsCortexImportEndpointNames endPointName = iPartsCortexImportEndpointNames.UNKNOWN;
        if (type == iPartsWSWorkBasketItem.TYPE.SAA) {
            endPointName = iPartsCortexImportEndpointNames.SAA_REMARKS;
        } else if (type == iPartsWSWorkBasketItem.TYPE.KEM) {
            endPointName = iPartsCortexImportEndpointNames.KEM_REMARKS;
        } else {
            // Kann wegen der Validierung gar nicht passieren.
            fireExceptionLogErrors(logHelper, "Unbekannter Typ", null);
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
            throwBadRequestError(WSError.INTERNAL_ERROR, "Unknown type", null);
        }
        return endPointName;
    }

    /**
     * Erzeugt aus dem übergebenen Bemerkungs-Objekt {@link iPartsWSWorkBasketRemarkItem} ein Bemerkungs-Datenbankobjekt {@link iPartsDataNutzDokRemark}
     *
     * @param project
     * @param workBasketRemarkItem
     * @param logHelper
     * @return
     */
    private iPartsDataNutzDokRemark handleWorkBasketRemarkItem(EtkProject project, iPartsWSWorkBasketRemarkItem workBasketRemarkItem,
                                                               ImportExportLogHelper logHelper) {
        String refId = workBasketRemarkItem.getRefId();
        if (workBasketRemarkItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.SAA) {
            refId = getRetailSaaInDbFormat(refId);
        }
        iPartsNutzDokRemarkId nutzDokRemarkId = new iPartsNutzDokRemarkId(refId, workBasketRemarkItem.getType(),
                                                                          workBasketRemarkItem.getId());
        iPartsDataNutzDokRemark nutzDokRemark = new iPartsDataNutzDokRemark(project, nutzDokRemarkId);
        boolean existedInDB = initIfNotExists(nutzDokRemark);
        fillNutzDokRemarkAttributes(workBasketRemarkItem, nutzDokRemark);
        if (existedInDB) {
            if (!logAlreadyExistsMessage(nutzDokRemark, logHelper, false)) {
                return null;
            }
        }

        // Prüfen, ob es überhaupt einen Arbeitsauftrag für die SAA bzw. KEM gibt
        String wordBasketItemNotFoundMessage = null;
        if (workBasketRemarkItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.KEM) {
            // KEM Arbeitsvorrat
            iPartsNutzDokKEMId nutzDokKEMId = new iPartsNutzDokKEMId(refId);
            iPartsDataNutzDokKEM nutzDokKEM = new iPartsDataNutzDokKEM(project, nutzDokKEMId);
            if (!nutzDokKEM.existsInDB()) {
                wordBasketItemNotFoundMessage = logHelper.translateForLog("!!Es existiert kein Arbeitsauftrag für die KEM \"%1\".",
                                                                          nutzDokKEMId.getKEMNo());
            }
        } else if (workBasketRemarkItem.getTypeAsEnum() == iPartsWSWorkBasketItem.TYPE.SAA) {
            // SAA Arbeitsvorrat
            iPartsNutzDokSAAId nutzDokSAAId = new iPartsNutzDokSAAId(refId);
            iPartsDataNutzDokSAA nutzDokSAA = new iPartsDataNutzDokSAA(project, nutzDokSAAId);
            if (!nutzDokSAA.existsInDB()) {
                wordBasketItemNotFoundMessage = logHelper.translateForLog("!!Es existiert kein Arbeitsauftrag für die SAA \"%1\".",
                                                                          nutzDokSAAId.getSAANo());
            }
        } else {
            // Kann wegen der Validierung gar nicht passieren.
            throwBadRequestError(WSError.INTERNAL_ERROR, "Unknown type", null);
            return null;
        }

        if (wordBasketItemNotFoundMessage != null) {
            wordBasketItemNotFoundMessage += " " + logHelper.translateForLog("!!Die Bemerkung wird trotzdem importiert.");
            logHelper.addLogWarning(wordBasketItemNotFoundMessage);
        }

        return nutzDokRemark;
    }

    /**
     * Befüllt das Bemerkungs-Datenbank-Objekt mit den Informationen aus der Bemerkung {@link iPartsWSWorkBasketRemarkItem}
     *
     * @param workBasketRemarkItem
     * @param nutzDokRemark
     */
    private void fillNutzDokRemarkAttributes(iPartsWSWorkBasketRemarkItem workBasketRemarkItem, iPartsDataNutzDokRemark nutzDokRemark) {
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
}