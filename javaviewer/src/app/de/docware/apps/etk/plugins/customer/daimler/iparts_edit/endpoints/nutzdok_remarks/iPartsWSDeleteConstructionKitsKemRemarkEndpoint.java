/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsNutzDokRemarkId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportMethod;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointNutzDok;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Path;
import de.docware.framework.modules.webservice.restful.annotations.PathParam;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.DELETE;

/**
 * Endpoint für DELETE constructionKits annotation KEM
 * <p>
 * DELETE [BaseURL]/iPartsEdit/constructionKits/KEM/{parent_id}/annotation/{id}
 * <p>
 * Beispiel:
 * DELETE /constructionKits/KEM/ZAM34219N03/annotation/18762
 * würde den Bemerkungstext mit der ID 18762 zur KEM "ZAM34219N03" löschen.
 */
public class iPartsWSDeleteConstructionKitsKemRemarkEndpoint extends iPartsWSAbstractEndpointNutzDok<iPartsWSDeleteConstructionKitsRemarkRequest> {

    public static final String ENDPOINT_URI_SUFFIX = "/KEM";

    public iPartsWSDeleteConstructionKitsKemRemarkEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @DELETE
    @Path("{parentId}/annotation/{remarkId}")
    @Produces({ MimeTypes.MIME_TYPE_JSON })
    public RESTfulTransferObjectInterface deleteAnnotation(@PathParam("parentId") String parentId, @PathParam("remarkId") String remarkId) {
        iPartsWSDeleteConstructionKitsRemarkRequest request = new iPartsWSDeleteConstructionKitsRemarkRequest();
        request.setItemType(iPartsWSWorkBasketItem.TYPE.KEM);
        request.setParentId(parentId);
        request.setRemarkId(remarkId);
        return handleWebserviceRequestIntern(request);
    }

    /**
     * Die Logik des Webservices, einschließlich dem Schreiben einer Job-Log-Datei.
     *
     * @param project
     * @param requestObject Anfrage-Objekt (wurde bereits von einem JSON-String deserialisiert)
     * @return
     * @throws RESTfulWebApplicationException
     */
    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSDeleteConstructionKitsRemarkRequest requestObject) throws RESTfulWebApplicationException {

        // Ein Job-Logfile schreiben für den Webservice-Aufruf.
        ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("Delete constructionKits annotation (KEM) WebService");

        CortexResult cortexResult = createAndSaveCortexElement(project, logHelper, requestObject,
                                                               iPartsCortexImportEndpointNames.KEM_REMARKS,
                                                               iPartsCortexImportMethod.DELETE);
        if (cortexResult == CortexResult.OK_STOP) {
            // keine weitere WebService-Endpoint Logik
            iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
        } else if (cortexResult == CortexResult.ERROR) {
            // es ist ein Fehler aufgetreten beim Speichern des CortexRecordsImports
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
        } else {
            // der ehemalige WebService Endpoint
            String logMessage = "";
            try {
                String kemNo = requestObject.getParentId();
                iPartsNutzDokRemarkId remarkId = new iPartsNutzDokRemarkId(kemNo, requestObject.getItemType().name(), requestObject.getRemarkId());
                iPartsDataNutzDokRemark remark = new iPartsDataNutzDokRemark(project, remarkId);
                if (!remark.existsInDB()) {
                    // Falls der Bemekerungstext nicht gefunden wurde, zunächst überprüfen, ob überhaupt einen Arbeitsauftrag
                    // für die KEM in der DB existiert
                    iPartsNutzDokKEMId nutzDokSAAId = new iPartsNutzDokKEMId(kemNo);
                    iPartsDataNutzDokKEM nutzDokSAA = new iPartsDataNutzDokKEM(project, nutzDokSAAId);
                    if (!nutzDokSAA.existsInDB()) {
                        // Logfile-Eintrag
                        logMessage = logHelper.translateForLog("!!Es existiert kein Arbeitsauftrag für die KEM \"%1\".",
                                                               kemNo);

                        // HTTP-Response
                        throwError(HttpConstants.HTTP_STATUS_NOT_FOUND, WSError.REQUEST_OBJECT_NOT_FOUND, "No KEM work basket item found for '"
                                                                                                          + kemNo + "'", null);
                    }

                    // Falls ein Arbeitsauftrag für die KEM in der DB existiert, gibt es die Bemerkungstext-ID nicht
                    logMessage = logHelper.translateForLog("!!Keine Bemerkung für KEM \"%1\" und Bemerkungs-ID \"%2\" gefunden.",
                                                           kemNo, requestObject.getRemarkId());
                    throwError(HttpConstants.HTTP_STATUS_NOT_FOUND, WSError.REQUEST_OBJECT_NOT_FOUND, "No remark found for KEM '"
                                                                                                      + kemNo + "' and remark id '"
                                                                                                      + requestObject.getRemarkId() + "'", null);
                } else {
                    // Hier wird gelöscht ...
                    remark.deleteFromDB();
                    // ... und ohne noch eimal zu lesen angenommen, dass das Löschen geklappt hat.
                    logHelper.addLogMsgWithTranslation("!!Bemerkung für KEM \"%1\" und Bemerkungs-ID \"%2\" erfolgreich gelöscht.",
                                                       kemNo, requestObject.getRemarkId());
                }

                // Das Logfile nach "processed" verschieben.
                iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
            } catch (RuntimeException e) {
                // Falls ein Fehler auftritt, diesen erst abfangen, das Job-Logfile definiert beenden/verschieben und die Exception weiterwerfen.
                // Wenn eine eigene Meldung existiert, diese ausgeben.
                fireExceptionLogErrors(logHelper, logMessage, e);
                // Das Logfile nach "error" verschieben ...
                iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());

                // ... und die Exception weiter werfen.
                throw e;
            }
        }
        return null;
    }
}
