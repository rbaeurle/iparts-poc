/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.EDSModelGroupUpdateImporter;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die Baumuster-Ausführungsarten aus TruckBOM.foundation
 * <p>
 * DAIMLER-13399: Importer für den getmodeltype WS wird fürs Erste nicht umgesetzt!
 */
public class TruckBOMModelTypeImporter extends AbstractTruckBOMFoundationJSONImporter {

    public TruckBOMModelTypeImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_MODEL_TYPE_IMPORT_NAME, TABLE_DA_OPS_GROUP);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_OPS_GROUP, new EDSModelGroupUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected boolean importJSONResponse(String response) {
        return false;
    }
}
