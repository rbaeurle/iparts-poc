/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;

import java.util.EnumSet;

/**
 * RelatedInfo für die Fußnoten
 */
public class iPartsRelatedInfoFootNote extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoFootNote() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA, iPartsConst.RELATED_INFO_FOOT_NOTE_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE));

    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        if (dataConnector.getOwnerConnector() instanceof EditModuleFormIConnector) {
            return new iPartsRelatedInfoInlineEditFootnoteDataForm(dataConnector, parentForm, this, dataConnector.isEditContext());
        } else {
            return new iPartsRelatedInfoFootNoteDataForm(dataConnector, parentForm, this);
        }
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }


    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
            EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
            if (AbstractRelatedInfoPartlistDataForm.isEditContext(dataConnector, false)) {
                // Im Edit immer sichtbar bei Stücklisteneinträgen von editierbaren Stücklisten (nur dort können Fußnoten
                // bearbeitet werden)
                EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
                if ((assembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)assembly).isPartListEditable()) {
                    if (iPartsUserSettingsHelper.isSingleEdit(partListEntry.getEtkProject())) {
                        return RelatedInfoDisplayState.ENABLED;
                    } else {
                        return RelatedInfoDisplayState.HIDE;
                    }
                }
            }

            // Es können nur echte Teile Fußnoten haben
            if (iPartsRelatedInfoFootNoteDataForm.relatedInfoIsVisible(partListEntry)) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
