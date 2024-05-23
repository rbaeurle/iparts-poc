/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartslist;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.util.file.DWFile;

/**
 * Service-Klasse für den GetPartsList Webservice und den dazugehörigen Importer der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetPartsListHelper implements iPartsTruckBOMFoundationWSWithImporter {

    public static final String WEBSERVICE_NAME = "getpartslist";

    @Override
    public synchronized AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile) {
        if (iPartsPlugin.isImportPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMPartsListImporter importer
                    = new de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMPartsListImporter(project);
            importer.startJSONImportFromTruckBOMResponse(response, jobLogFile);
            return importer;
        }
        return null;
    }

    @Override
    public String getWebServiceName() {
        return WEBSERVICE_NAME;
    }

    @Override
    public iPartsTruckBOMFoundationWSGetPartsListRequest createRequest(String identifier) {
        return new iPartsTruckBOMFoundationWSGetPartsListRequest(identifier);
    }
}