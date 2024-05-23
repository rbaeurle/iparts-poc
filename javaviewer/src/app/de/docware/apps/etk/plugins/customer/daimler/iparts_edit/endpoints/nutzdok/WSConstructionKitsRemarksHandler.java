package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsCortexImportEndpointNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsDataCortexImport;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;

/**
 * Handler des Cortex Schedulers für KEM_REMARKS (Insert und Delete)
 */
public class WSConstructionKitsRemarksHandler extends AbstractWSConstructionKitsHandler {

    public WSConstructionKitsRemarksHandler(EtkProject project) {
        super(project, iPartsCortexImportEndpointNames.KEM_REMARKS);
    }

    public WSConstructionKitsRemarksHandler(EtkProject project, ImportExportLogHelper logHelper) {
        super(project, iPartsCortexImportEndpointNames.KEM_REMARKS, logHelper);
    }

    @Override
    protected boolean doBeforeLogic() {
        return true;
    }

    /**
     * Die eigentliche Ausführung des KEM_REMARKS Handlers
     *
     * @param dataCortexImport
     * @return
     */
    @Override
    protected boolean doExecute(iPartsDataCortexImport dataCortexImport) {
        return handleInsertDeleteKitsRemarkItem(dataCortexImport, iPartsWSWorkBasketItem.TYPE.KEM);
    }

}
