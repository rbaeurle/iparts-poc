/*
 * Copyright (c) 2016 Docware GmbH
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

import java.util.EnumSet;

/**
 * RelatedInfo für die zugeordneten Bildaufträge zu einer Sachnummer.
 */
public class iPartsRelatedInfoPicOrdersToPartData extends EtkRelatedInfoBaseImpl {

    public static final boolean isBlocked = true;  // DAIMLER-7902 "Bildauftragszuordnung zu Teil" soll nicht mehr angezeigt werden

    public iPartsRelatedInfoPicOrdersToPartData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_PICORDERS_TO_PART_DATA, iPartsConst.RELATED_INFO_PICORDERS_TO_PART_DATA_TEXT, true, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoPicOrdersToPartDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (isBlocked) {
            return RelatedInfoDisplayState.HIDE;
        } else {
            if (dataConnector.getRelatedInfoData().isAssembly() || !dataConnector.getRelatedInfoData().getNavigationPath().isEmpty()) {
                // Es können nur echte Teile im Bildauftrag verwendet werden
                return RelatedInfoDisplayState.HIDE;
            } else {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getAsPartListEntryId().getOwnerAssemblyId());
                if (!iPartsRelatedInfoPicOrdersToPartDataForm.relatedInfoIsVisible(assembly, dataConnector)) {
                    return RelatedInfoDisplayState.HIDE;
                }
            }
            return RelatedInfoDisplayState.ENABLED;
        }
    }
}
