package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;

/**
 * Handler für SA-Stücklisten aus SAP-MBS
 */
public class MBSPartsListNumberHandler extends AbstractMBSSAAMasterDataHandler {

    private static final String TRIGGER_ELEMENT = "PartsListMasterData";

    public MBSPartsListNumberHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS SA-Stückliste-Stammdaten");
    }

    @Override
    protected String getSpecificXMLElementForNumber() {
        return PARTS_LIST_NUMBER;
    }
}
