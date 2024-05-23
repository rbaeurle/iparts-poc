/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAutoTransferPartlistGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.sql.SQLStringConvert;

import java.util.List;

/**
 * Diese Klasse enthält sämtliche Metadaten, die für das Formular und die eigentliche automatische Übernahme aus der Konstruktion
 * in AS notwendig sind.
 */
public class RowContentForAutoTransferToAS {

    private AssemblyId assemblyId;
    private List<TransferToASElement> transferElements;
    private boolean transferMark;

    public DBDataObjectAttributes getAsAttributes(EtkProject project) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.addField(EditAutoTransferPartlistGrid.FIELD_PSEUDO_ASSEMBLY_ID, getAssemblyId().getKVari(), DBActionOrigin.FROM_DB);
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, getAssemblyId());
        String assemblyName = assembly.getPart().getFieldValue(iPartsConst.FIELD_M_TEXTNR, project.getDBLanguage(), true);
        attributes.addField(EditAutoTransferPartlistGrid.FIELD_PSEUDO_ASSEMBLY_NAME, assemblyName, DBActionOrigin.FROM_DB);
        attributes.addField(EditAutoTransferPartlistGrid.FIELD_PSEUDO_TRANSFER, SQLStringConvert.booleanToPPString(isTransferMark()),
                            DBActionOrigin.FROM_DB);
        return attributes;
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    public void setAssemblyId(AssemblyId assemblyId) {
        this.assemblyId = assemblyId;
    }

    public List<TransferToASElement> getTransferElements() {
        return transferElements;
    }

    public void setTransferElements(List<TransferToASElement> transferElements) {
        this.transferElements = transferElements;
    }

    public boolean isTransferMark() {
        return transferMark;
    }

    public void setTransferMark(boolean transferMark) {
        this.transferMark = transferMark;
    }
}
