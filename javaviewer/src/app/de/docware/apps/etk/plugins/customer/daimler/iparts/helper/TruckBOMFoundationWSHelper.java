/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import com.owlike.genson.stream.JsonStreamException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getcode.iPartsTruckBOMFoundationWSGetCodeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodel.iPartsTruckBOMFoundationWSGetModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodelemelentusage.iPartsTruckBOMFoundationWSGetModelElementUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodeltype.iPartsTruckBOMFoundationWSGetModelTypeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodulecategory.iPartsTruckBOMFoundationWSGetModuleCategoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpart.iPartsTruckBOMFoundationWSGetPartHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartslist.iPartsTruckBOMFoundationWSGetPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartusage.iPartsTruckBOMFoundationWSGetPartUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsparepartusage.iPartsTruckBOMFoundationWSGetSparePartUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsubmodulecategory.iPartsTruckBOMFoundationWSGetSubModuleCategoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.JSONUtils;

import java.util.*;

/**
 * Helfer zum Verwalten der TruckBOM.foundation Webservices inkl. Importer
 */
public class TruckBOMFoundationWSHelper {

    private Map<String, iPartsTruckBOMFoundationWSWithImporter> postRequestWebServices; // Map mit Ziel-Webservice-URI auf Hilfsklasse
    private Set<String> nonImportWebServices; // Webservice, die keinen Import haben
    private Genson genson = JSONUtils.createGenson(true);

    public TruckBOMFoundationWSHelper() {
        fillWebServices();
    }

    /**
     * Alle Import-WebServices zu ihren URIs ablegen
     */
    private void fillWebServices() {
        postRequestWebServices = new TreeMap<>();
        nonImportWebServices = new HashSet<>();
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetPartHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetPartsListHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetPartUsageHelper());
        addNonImportWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetCodeHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetModelHelper());
        addNonImportWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetModelTypeHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetModuleCategoryHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetSubModuleCategoryHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetSparePartUsageHelper());
        addWebServiceImportHelper(new iPartsTruckBOMFoundationWSGetModelElementUsageHelper());
    }

    private void addWebServiceImportHelper(iPartsTruckBOMFoundationWSWithImporter wsImportHelper) {
        postRequestWebServices.put(wsImportHelper.getWebServiceName(), wsImportHelper);
    }

    private void addNonImportWebServiceImportHelper(iPartsTruckBOMFoundationWSWithImporter wsImportHelper) {
        addWebServiceImportHelper(wsImportHelper);
        nonImportWebServices.add(wsImportHelper.getWebServiceName());
    }

    /**
     * Liefert die URIs zu allen Webservices
     *
     * @return
     */
    public Set<String> getWebserviceURIs() {
        return postRequestWebServices.keySet();
    }

    /**
     * Liefert die URIs zu allen Webservices, die einen Importer haben
     *
     * @return
     */
    public Set<String> getWebserviceWithImporterURIs() {
        Set<String> wsNames = new TreeSet<>(postRequestWebServices.keySet());
        wsNames.removeAll(nonImportWebServices);
        return wsNames;
    }

    /**
     * Liefert den {@link iPartsTruckBOMFoundationWSWithImporter} zum übergebenen Webservice Namen
     *
     * @param webserviceName
     * @return
     */
    public iPartsTruckBOMFoundationWSWithImporter getHelperForWSName(String webserviceName) {
        return postRequestWebServices.get(webserviceName);
    }

    public boolean isNonImportWebservice(String selectedWebservice) {
        return nonImportWebServices.contains(selectedWebservice);
    }

    public boolean isPostRequestWebservice(String selectedWebservice) {
        return getWebserviceURIs().contains(selectedWebservice);
    }

    /**
     * Methode zum Initialisieren und Befüllen des Request Payloads
     *
     * @param webserviceWithImporter
     * @param fromDateTime
     * @param toDateTime
     * @return
     */

    private AbstractiPartsTruckBOMFoundationWebserviceRequest initAndFillRequestPayload(iPartsTruckBOMFoundationWSWithImporter webserviceWithImporter,
                                                                                        String fromDateTime, String toDateTime) {
        // Request-Payload aufbauen
        AbstractiPartsTruckBOMFoundationWebserviceRequest requestPayload = webserviceWithImporter.createRequest("*");
        requestPayload.setReleasedLaterThan(fromDateTime);
        requestPayload.setReleasedEarlierThan(toDateTime);
        return requestPayload;
    }

    public String createRequestPayload(iPartsTruckBOMFoundationWSWithImporter webserviceWithImporter,
                                       String fromDateTime, String toDateTime, LogChannels logChannel) {
        // Request Payload in Abhängigkeit des Webservices initialisieren und befüllen
        // Denkbar ist auch ein gemeinsamer Payload für alle Services zu nutzen (evtl. Performance-Optimierung)
        AbstractiPartsTruckBOMFoundationWebserviceRequest requestPayload = initAndFillRequestPayload(webserviceWithImporter,
                                                                                                     fromDateTime,
                                                                                                     toDateTime);
        try {
            // JSON-Validität des Request Payload prüfen
            return genson.serialize(requestPayload);
        } catch (JsonStreamException | JsonBindingException e) {
            Logger.log(logChannel, LogType.ERROR,
                       "Request payload could not be serialized to a valid JSON string");
        }
        return null;
    }
}
