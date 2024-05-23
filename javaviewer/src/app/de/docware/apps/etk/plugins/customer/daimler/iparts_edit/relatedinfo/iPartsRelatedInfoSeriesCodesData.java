/*
 * Copyright (c) 2019 Docware GmbH
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
 * RelatedInfo für die Series-Codes: Anzeige Baubarkeit (X4E) zur Baureihe
 */
public class iPartsRelatedInfoSeriesCodesData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoSeriesCodesData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_SERIES_CODES_DATA, iPartsConst.RELATED_INFO_SERIES_CODES_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoSeriesCodesDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getSachAssemblyId());
        if (iPartsRelatedInfoSeriesCodesDataForm.isValidForSeriesCodeRelInfo(assembly)) {
            return RelatedInfoDisplayState.ENABLED;
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
