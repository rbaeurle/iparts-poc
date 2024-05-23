/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;

import java.util.EnumSet;

/**
 * RelatedInfo für die Werkseinsatzdaten
 */
public class iPartsRelatedInfoFactoryData extends EtkRelatedInfoBaseImpl {

    private boolean scaleFromParent;

    public iPartsRelatedInfoFactoryData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_FACTORY_DATA, iPartsConst.RELATED_INFO_FACTORY_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
        scaleFromParent = false;
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoFactoryDataForm(dataConnector, parentForm, this, scaleFromParent);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            // Es können nur echte Teile Werksdaten haben
            return RelatedInfoDisplayState.HIDE;
        } else if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            if (!iPartsRelatedInfoFactoryDataForm.relatedInfoIsVisible(dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()),
                                                                       AbstractRelatedInfoPartlistDataForm.isEditContext(dataConnector, false),
                                                                       dataConnector)) {
                return RelatedInfoDisplayState.HIDE;
            }
        }

        return RelatedInfoDisplayState.ENABLED;
    }

    public void setScaleFromParent(boolean scaleFromParent) {
        this.scaleFromParent = scaleFromParent;
    }

}
