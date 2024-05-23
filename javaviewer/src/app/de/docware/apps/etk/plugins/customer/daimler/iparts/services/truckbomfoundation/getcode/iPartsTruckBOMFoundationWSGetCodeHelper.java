/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getcode;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.util.file.DWFile;

/**
 * Service-Klasse für den GetCode Webservice der TruckBOM.foundation (dafür gibt es keinen Importer)
 */
public class iPartsTruckBOMFoundationWSGetCodeHelper implements iPartsTruckBOMFoundationWSWithImporter {

    public static final String WEBSERVICE_NAME = "getcode";

    @Override
    public AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile) {
        // Die Code-Daten werden nicht importiert, sondern für die Anzeige der Daten verwendet
        return null;
    }

    @Override
    public String getWebServiceName() {
        return WEBSERVICE_NAME;
    }

    @Override
    public AbstractiPartsTruckBOMFoundationWebserviceRequest createRequest(String identifier) {
        return new iPartsTruckBOMFoundationWSGetCodeRequest();
    }
}