package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;

/**
 * Handler für CTT Stücklisten aus SAP-MBS
 */
public class MBSCTTListDataHandler extends AbstractMBSSAAMasterDataHandler {

    private static final String TRIGGER_ELEMENT = "CTTListMasterData";

    public MBSCTTListDataHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS CTT-Stammdaten");
    }


    @Override
    protected String getNumberFromSpecificElement() {
        // Die Nummer aus dem aktuellen Importdatensatz bestimmen und dann das "Z" hinzufügen
        String cttListNumber = super.getNumberFromSpecificElement();

        // returnDifferentRetailSAA muss false sein, weil beim Aufruf von getNumberFromSpecificElement() die Original-SAA
        // zurückgegeben werden muss; eine evtl. abweichende Retail-SAA wird später dann daraus berechnet
        return getImportHelper().addSaaPrefixIfNeeded(cttListNumber, false);
    }

    @Override
    protected String getSpecificXMLElementForNumber() {
        return CTT_LIST_NUMBER;
    }
}
