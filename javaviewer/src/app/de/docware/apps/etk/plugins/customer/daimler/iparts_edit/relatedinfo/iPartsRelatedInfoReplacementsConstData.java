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
 * RelatedInfo für ersetzte Materialien, Vorgänger und Nachfolger.
 */
public class iPartsRelatedInfoReplacementsConstData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoReplacementsConstData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_REPLACE_CONST_MAT_DATA, iPartsConst.RELATED_INFO_REPLACE_CONST_MAT_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoReplacementsConstDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            // Es können nur echte Teile Ersetzungen haben
            if (iPartsRelatedInfoReplacementsConstDataForm.relatedInfoIsVisible(dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()))) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
