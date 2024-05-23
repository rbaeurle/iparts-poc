/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: tauber
 * Date: 01.02.18
 * Time: 08:49
 * To change this template use File | Settings | File Templates.
 */
public class iPartsRelatedInfoConstructionKitData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoConstructionKitData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_CONSTRUCTION_KITS_DATA, iPartsConst.RELATED_INFO_CONSTRUCTIONS_KITS_TEXT, true, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoConstructionKitDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            // Es können nur echte Teile Baukästen haben
            return RelatedInfoDisplayState.HIDE;
        } else if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            if (!iPartsRelatedInfoConstructionKitDataForm.relatedInfoIsVisible(dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()),
                                                                               dataConnector)) {
                return RelatedInfoDisplayState.HIDE;
            }
        }

        return RelatedInfoDisplayState.ENABLED;
    }
}
