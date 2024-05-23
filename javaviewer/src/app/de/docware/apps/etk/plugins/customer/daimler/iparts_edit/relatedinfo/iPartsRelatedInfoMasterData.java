/*
 * Copyright (c) 2015 Docware GmbH
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
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;

import java.util.EnumSet;

/**
 * RelatedInfo für die Stammdaten
 */
public class iPartsRelatedInfoMasterData extends EtkRelatedInfoBaseImpl {


    public iPartsRelatedInfoMasterData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_MASTER_DATA, iPartsConst.RELATED_INFO_MASTER_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));

    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        // Hier werden die Module bearbeitet.
        EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
        if (dataConnector.getRelatedInfoData().isAssembly() || ((partListEntry != null) && partListEntry.isAssembly())) {
            EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getSachAssemblyId());

            // getLastHiddenSingleSubAssemblyOrThis() ist notwendig für ausgeblendete Modul-Knoten im Baugruppenbaum
            currentAssembly = currentAssembly.getLastHiddenSingleSubAssemblyOrThis(null);

            if ((currentAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)currentAssembly).isPartListEditable()) {
                return new iPartsRelatedInfoEditModuleDataForm(dataConnector, parentForm, this);
            }
        } else {
            // Hier sollen nur echte Materialien bearbeitet werden können.
            if (dataConnector.isEditContext() && iPartsEditPlugin.isMaterialEditable()) {
                if ((partListEntry != null) && !partListEntry.isAssembly()) {
                    EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
                    if ((ownerAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)ownerAssembly).isPartListEditable()) {
                        return new iPartsRelatedInfoEditMaterialDataForm(dataConnector, parentForm, this);
                    }
                }
            }
        }
        return new iPartsRelatedInfoMasterDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

}
