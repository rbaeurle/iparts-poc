package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;

/**
 * Handler für SA-KG-Stücklisten aus SAP-MBS
 */
public class MBSPartsListConGroupNumberHandler extends AbstractMBSSAAMasterDataHandler {

    private static final String TRIGGER_ELEMENT = "PartsListConGroupMasterData";

    public MBSPartsListConGroupNumberHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS SA-KG Stückliste-Stammdaten");
    }

    @Override
    protected String getSpecificXMLElementForNumber() {
        return PARTS_LIST_CON_GROUP_NUMBER;
    }
}
