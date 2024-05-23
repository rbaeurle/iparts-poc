package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Related Edit für die Daten von einem Stücklisteneintrag inkl. Fußnoten und Wahlweise.
 */
public class iPartsRelatedInfoEditDetailsData extends AbstractSuperEditRelatedInfo {

    public iPartsRelatedInfoEditDetailsData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA, iPartsConst.RELATED_INFO_EDIT_DETAILS_DATA_TEXT);
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoSuperEditDataForm(dataConnector, parentForm, this, iPartsRelatedInfoSuperEditDataForm.PartListEditType.PART_LIST_ENTRY_DATA);
    }
}
