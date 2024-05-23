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
 * RelatedInfo für die Primus Ersatzkette
 */
public class iPartsRelatedInfoPrimusReplacementChain extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoPrimusReplacementChain() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA, iPartsConst.RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));

    }

    public iPartsRelatedInfoPrimusReplacementChain(String name, String displayText, boolean partSpecific, boolean defaultVisible, EnumSet<RelatedInfoDisplayOption> displayOptions) {
        super(name, displayText, partSpecific, defaultVisible, displayOptions);
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoPrimusReplacementChainForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            // Es können nur echte Teile Primus Ersetzungen haben
            return RelatedInfoDisplayState.HIDE;
        } else {
            EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
            if (!iPartsRelatedInfoPrimusReplacementChainForm.relatedInfoIsVisible(partListEntry, dataConnector.getProject())) {
                return RelatedInfoDisplayState.HIDE;
            }
        }
        return RelatedInfoDisplayState.ENABLED;
    }

}
