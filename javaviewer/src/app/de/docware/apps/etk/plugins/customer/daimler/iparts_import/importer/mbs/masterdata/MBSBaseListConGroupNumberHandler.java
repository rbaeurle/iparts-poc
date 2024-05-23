package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;

/**
 * Handler für Grundstücklisten mit KG aus SAP-MBS
 */
public class MBSBaseListConGroupNumberHandler extends AbstractMBSSAAMasterDataHandler {

    private static final String TRIGGER_ELEMENT = "BaseListConGroupMasterData";

    public MBSBaseListConGroupNumberHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS Grundstückliste (KG)-Stammdaten");
    }

    @Override
    protected String getSpecificXMLElementForNumber() {
        return BASE_LIST_CON_GROUP_NUMBER;
    }
}
