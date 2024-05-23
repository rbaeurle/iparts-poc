/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

import java.util.EnumSet;

/**
 * RelatedInfo für die SA-Stammdaten
 */
public class iPartsRelatedInfoSAMasterData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoSAMasterData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_SA_MASTER_DATA, iPartsConst.RELATED_INFO_SA_MASTER_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));

    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoMasterDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
        if (dataConnector.getRelatedInfoData().isAssembly() || ((partListEntry != null) && partListEntry.isAssembly())) {
            EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getSachAssemblyId());

            // getLastHiddenSingleSubAssemblyOrThis() ist notwendig für ausgeblendete Modul-Knoten im Baugruppenbaum
            currentAssembly = currentAssembly.getLastHiddenSingleSubAssemblyOrThis(null);

            if (iPartsRelatedInfoMasterDataForm.relatedInfoIsVisibleForSA(currentAssembly)) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }

        return RelatedInfoDisplayState.HIDE;
    }
}
