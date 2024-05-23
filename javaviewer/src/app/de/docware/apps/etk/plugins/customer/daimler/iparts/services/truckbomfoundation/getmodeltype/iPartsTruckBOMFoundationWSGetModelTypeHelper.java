/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodeltype;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.util.file.DWFile;


/**
 * Service-Klasse für den GetModelType Webservice und den dazugehörigen Importer der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetModelTypeHelper implements iPartsTruckBOMFoundationWSWithImporter {

    public static final String WEBSERVICE_NAME = "getmodeltype";

    @Override
    public AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile) {
        // DAIMLER-13399: Importer für den getmodeltype WS wird fürs Erste nicht umgesetzt
        return null;
    }

    @Override
    public String getWebServiceName() {
        return WEBSERVICE_NAME;
    }

    @Override
    public AbstractiPartsTruckBOMFoundationWebserviceRequest createRequest(String identifier) {
        return new iPartsTruckBOMFoundationWSGetModelTypeRequest(identifier);
    }

}
