package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;

import java.util.EnumSet;

/**
 * RelatedInfo für die DIALOG mehrstufigen Retail-Ersetzungskette
 */
public class iPartsRelatedInfoDialogMultistageRetailReplacementChain extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoDialogMultistageRetailReplacementChain(String name, String displayText, boolean partSpecific, boolean defaultVisible, EnumSet<RelatedInfoDisplayOption> displayOptions) {
        super(name, displayText, partSpecific, defaultVisible, displayOptions);
    }

    public iPartsRelatedInfoDialogMultistageRetailReplacementChain() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN, iPartsConst.RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoDialogMultistageRetailReplacementChainForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        // Muss hierüber gesteuert werden, da die RelatedInfoDisplayOption EDIT_CONTEXT_ONLY
        // den Aufruf in einem Read-Only TU verhindert
        boolean isEditContext = AbstractRelatedInfoPartlistDataForm.isEditContext(dataConnector, false);
        if (isEditContext) {
            // Es können nur echte Teile Ersetzungen haben
            if (dataConnector.getRelatedInfoData().isPartListEntryId()) {
                EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
                if (iPartsRelatedInfoDialogMultistageRetailReplacementChainForm.relatedInfoIsVisible(partListEntry)) {
                    return RelatedInfoDisplayState.ENABLED;
                }
            }
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
