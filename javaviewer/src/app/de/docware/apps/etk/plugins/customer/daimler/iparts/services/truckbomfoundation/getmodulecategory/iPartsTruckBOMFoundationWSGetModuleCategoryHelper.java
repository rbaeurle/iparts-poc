/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodulecategory;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.util.file.DWFile;


/**
 * Service-Klasse für den getmodulecategory Webservice und den dazugehörigen Importer der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetModuleCategoryHelper implements iPartsTruckBOMFoundationWSWithImporter {

    public static final String WEBSERVICE_NAME = "getmodulecategory";

    @Override
    public AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile) {
        if (iPartsPlugin.isImportPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMModuleCategoryImporter importer
                    = new de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer.TruckBOMModuleCategoryImporter(project,
                                                                                                                                                             iPartsEdsStructureHelper.getInstance().isNewStructureActive());
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
    public AbstractiPartsTruckBOMFoundationWebserviceRequest createRequest(String identifier) {
        return new iPartsTruckBOMFoundationWSGetModuleCategoryRequest(identifier);
    }
}
