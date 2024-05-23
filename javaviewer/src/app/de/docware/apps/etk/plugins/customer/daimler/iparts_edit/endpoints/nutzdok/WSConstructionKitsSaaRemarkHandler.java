package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsDataCortexImport;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;

/**
 * Handler des Cortex Schedulers für SAA_REMARKS (Insert und Delete)
 */
public class WSConstructionKitsSaaRemarkHandler extends AbstractWSConstructionKitsHandler {

    public WSConstructionKitsSaaRemarkHandler(EtkProject project) {
        super(project, iPartsCortexImportEndpointNames.SAA_REMARKS);
    }

    public WSConstructionKitsSaaRemarkHandler(EtkProject project, ImportExportLogHelper logHelper) {
        super(project, iPartsCortexImportEndpointNames.SAA_REMARKS, logHelper);
    }

    @Override
    protected boolean doBeforeLogic() {
        return true;
    }

    /**
     * Die eigentliche Ausführung des SAA_REMARKS Handlers
     *
     * @param dataCortexImport
     * @return
     */
    @Override
    protected boolean doExecute(iPartsDataCortexImport dataCortexImport) {
        return handleInsertDeleteKitsRemarkItem(dataCortexImport, iPartsWSWorkBasketItem.TYPE.SAA);
    }
}
