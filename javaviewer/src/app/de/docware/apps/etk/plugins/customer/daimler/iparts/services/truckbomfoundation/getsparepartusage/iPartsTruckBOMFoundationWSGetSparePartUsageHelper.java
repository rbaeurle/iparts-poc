package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsparepartusage;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.util.file.DWFile;

/**
 * Service-Klasse für den getSparePartUsage Webservice und den dazugehörigen Importer der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetSparePartUsageHelper implements iPartsTruckBOMFoundationWSWithImporter {

    public static final String WEBSERVICE_NAME = "getsparepartusage";

    @Override
    public AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile) {
        if (iPartsPlugin.isImportPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMSparePartUsageImporter importer =
                    new de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMSparePartUsageImporter(project);
            importer.startJSONImportFromTruckBOMResponse(response, jobLogFile);
        }
        return null;
    }

    @Override
    public String getWebServiceName() {
        return WEBSERVICE_NAME;
    }

    @Override
    public AbstractiPartsTruckBOMFoundationWebserviceRequest createRequest(String identifier) {
        return new iPartsTruckBOMFoundationWSGetSparePartUsageRequest(identifier);
    }
}
