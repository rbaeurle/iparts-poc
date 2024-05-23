/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;

import java.util.EnumSet;
import java.util.List;

/**
 * RelatedInfo für die Baumusterstammdaten
 */
public class iPartsRelatedInfoModelMasterData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoModelMasterData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_MODEL_MASTER_DATA, iPartsConst.RELATED_INFO_MODEL_MASTER_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoModelMasterDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getSachAssemblyId());
        if (assembly instanceof iPartsDataAssembly) {
            List<iPartsVirtualNode> vNodes = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
            if (iPartsVirtualNode.isSeriesNode(vNodes) || iPartsVirtualNode.isProductNode(vNodes)) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }
        return RelatedInfoDisplayState.HIDE;

    }
}
