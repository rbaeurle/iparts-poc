package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;

/**
 * Handler für Grundstücklisten aus SAP-MBS
 */
public class MBSBaseListNumberHandler extends AbstractMBSSAAMasterDataHandler {

    private static final String TRIGGER_ELEMENT = "BaseListMasterData";

    public MBSBaseListNumberHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS Grundstückliste-Stammdaten");
    }

    @Override
    protected String getSpecificXMLElementForNumber() {
        return BASE_LIST_NUMBER;
    }
}
