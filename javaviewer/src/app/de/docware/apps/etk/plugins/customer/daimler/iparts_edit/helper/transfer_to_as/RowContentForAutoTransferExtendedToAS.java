/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAutoTransferPartListExtendedGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAutoTransferPartlistGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.sql.SQLStringConvert;

import java.util.Map;

public class RowContentForAutoTransferExtendedToAS extends RowContentForAutoTransferToAS {

    private boolean isNewTu;

    @Override
    public DBDataObjectAttributes getAsAttributes(EtkProject project) {
        DBDataObjectAttributes attributes = super.getAsAttributes(project);
        if (isNewTu) {
            // Bei neuen TUs kommt die Benennung aus dem Template und muss extra gesetzt werden
            EditTransferToASHelper helper = new EditTransferToASHelper(project, null, null);
            // Wir sind immer in einer Zeile -> ProductId und KgTuId müssten für jedes TransferElement gleich sein
            TransferToASElement transferToASElement = getTransferElements().get(0);
            Map<String, KgTuListItem> kgTuStructure = KgTuHelper.getKGTUStructure(project, transferToASElement.getProductId());
            KgTuId kgTuId = transferToASElement.getKgTuId();
            KgTuListItem tuListItem = helper.getKgTuListItem(kgTuId, kgTuStructure);
            String assemblyName = "???";
            if ((tuListItem != null) && (tuListItem.getKgTuNode() != null) && (tuListItem.getKgTuNode().getTitle() != null)) {
                assemblyName = tuListItem.getKgTuNode().getTitle().getText(project.getDBLanguage());
            } else {
                KgTuForProduct kgTuProduct = KgTuForProduct.getInstance(project, transferToASElement.getProductId());
                KgTuNode searchKgTuNode = kgTuProduct.getTuNode(kgTuId.getKg(), kgTuId.getTu());
                if ((searchKgTuNode != null) && (searchKgTuNode.getTitle() != null)) {
                    assemblyName = searchKgTuNode.getTitle().getText(project.getDBLanguage());
                    setNewTu(false);
                } else {
                    // KG/TU-Knoten kann nicht angelegt werden (kein Template und TU existiert nicht)
                    setTransferMark(false);
                }
            }
            attributes.addField(EditAutoTransferPartlistGrid.FIELD_PSEUDO_ASSEMBLY_NAME, assemblyName, DBActionOrigin.FROM_DB);
        }
        attributes.addField(EditAutoTransferPartListExtendedGrid.FIELD_PSEUDO_NEW_TU, SQLStringConvert.booleanToPPString(isNewTu()), DBActionOrigin.FROM_DB);
        return attributes;
    }

    public boolean isNewTu() {
        return isNewTu;
    }

    public void setNewTu(boolean newTu) {
        isNewTu = newTu;
    }

}
