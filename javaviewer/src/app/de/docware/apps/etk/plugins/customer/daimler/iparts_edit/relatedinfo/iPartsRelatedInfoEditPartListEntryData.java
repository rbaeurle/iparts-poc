/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;

import java.util.EnumSet;

/**
 * Related Info zum Bearbeiten eines Stücklisteneintrags in iParts.
 */
public class iPartsRelatedInfoEditPartListEntryData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoEditPartListEntryData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_PART_LIST_ENTRY_DATA, iPartsConst.RELATED_INFO_EDIT_PART_LIST_ENTRY_DATA_TEXT,
              false, true, EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                                      RelatedInfoDisplayOption.EDIT_CONTEXT_ONLY,
                                      RelatedInfoDisplayOption.COMMONENTRY,
                                      RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoEditPartListEntryDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            // Es können nur bei Stücklisteneinträgen von editierbaren Stücklisten die Daten bearbeitet werden
            EtkDataAssembly assembly = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()).getOwnerAssembly();
            if ((assembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)assembly).isPartListEditable()) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }

        return RelatedInfoDisplayState.HIDE;
    }
}
