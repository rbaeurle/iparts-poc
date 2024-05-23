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
 * RelatedInfo für ersetzte Materialien, Vorgänger und Nachfolger.
 */
public class iPartsRelatedInfoReplacementsData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoReplacementsData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA, iPartsConst.RELATED_INFO_REPLACEMENTS_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoReplacementsDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            boolean isEditRelatedInfo = AbstractRelatedInfoPartlistDataForm.isEditContext(dataConnector, false);
            boolean isFilterActive = !isEditRelatedInfo && iPartsRelatedInfoFormConnectorWithFilterSettings.isFilterActive(dataConnector);
            // Es können nur echte Teile Ersetzungen haben
            if (iPartsRelatedInfoReplacementsDataForm.relatedInfoIsVisible(dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()),
                                                                           isEditRelatedInfo,
                                                                           isFilterActive)) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
