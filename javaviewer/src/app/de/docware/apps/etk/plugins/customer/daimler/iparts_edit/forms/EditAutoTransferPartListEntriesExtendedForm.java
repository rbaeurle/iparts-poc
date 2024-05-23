/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForAutoTransferExtendedToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForAutoTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.utils.VarParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditAutoTransferPartListEntriesExtendedForm extends EditAutoTransferPartlistEntriesForm {

    public static List<TransferToASElement> doAutoTransferToASPartlistExtended(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                                               Map<String, List<TransferToASElement>> moduleToTransferElementsMap,
                                                                               Map<String, TransferToASElement> notExistingModuleMap,
                                                                               iPartsProduct masterProduct, VarParam<Boolean> openModulesInEdit) {
        EditAutoTransferPartListEntriesExtendedForm form = new EditAutoTransferPartListEntriesExtendedForm(connector, parentForm);
        form.init(moduleToTransferElementsMap, notExistingModuleMap);
        if (masterProduct != null) {
            openModulesInEdit.setValue(false);
            return form.getAllTransferItems();
        }
        if (form.showModal() == ModalResult.OK) {
            openModulesInEdit.setValue(form.isShowModuleSelected());
            return form.getAllTransferItems();
        } else {
            return null;
        }
    }

    /**
     * Erzeugt eine Instanz von EditAutoTransferPartlistEntriesForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public EditAutoTransferPartListEntriesExtendedForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        // Bei der erweiterten automatischen Übernahme gibt es im Grid noch ein zusätzliches Feld
        // Grids austauschen
        EditAutoTransferPartlistGrid gridNew = new EditAutoTransferPartListExtendedGrid(getConnector(), this, EditAutoTransferPartlistGrid.TABLE_PSEUDO);
        EditAutoTransferPartlistGrid gridOld = getGrid();
        getContentTablePanel().removeChild(gridOld.getGui());
        setGrid(gridNew);
        getContentTablePanel().addChildBorderCenter(gridNew.getGui());
    }

    public void init(Map<String, List<TransferToASElement>> moduleToTransferElementsMap, Map<String, TransferToASElement> notExistingModuleMap) {
        // Checkbox TU öffnen soll selektiert sein
        setShowModuleSelected(true);

        List<RowContentForAutoTransferToAS> rowContents = new ArrayList<>(moduleToTransferElementsMap.size());
        for (Map.Entry<String, List<TransferToASElement>> transferEntry : moduleToTransferElementsMap.entrySet()) {
            RowContentForAutoTransferExtendedToAS rowContent = new RowContentForAutoTransferExtendedToAS();
            rowContent.setAssemblyId(new AssemblyId(transferEntry.getKey(), ""));
            rowContent.setTransferElements(transferEntry.getValue());
            rowContent.setTransferMark(true); // Initial alle übernehmen
            // Flag setzen, ob es den TU bereits gibt
            rowContent.setNewTu(notExistingModuleMap.containsKey(transferEntry.getKey()));
            rowContents.add(rowContent);
        }

        fillGrid(rowContents);
    }
}
