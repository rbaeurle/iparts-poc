/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

import java.util.EnumSet;

/**
 * RelatedInfo für die privaten Texte zu einem Stücklisteneintrag.
 */
public class iPartsRelatedInfoInternalTextForPartData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoInternalTextForPartData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA, iPartsConst.RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA_TEXT, true, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoInternalTextForPartDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            // Es können nur echte Teile mit privatem Text versorgt werden
            return RelatedInfoDisplayState.HIDE;
        } else {
            EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
            if (!iPartsRelatedInfoInternalTextForPartDataForm.relatedInfoIsVisible(partListEntry, dataConnector)) {
                return RelatedInfoDisplayState.HIDE;
            }
        }
        return RelatedInfoDisplayState.ENABLED;
    }
}
