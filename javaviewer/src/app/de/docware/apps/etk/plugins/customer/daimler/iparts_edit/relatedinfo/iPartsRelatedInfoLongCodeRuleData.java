/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

import java.util.EnumSet;

/**
 * RelatedInfo für Anzeige der Langen Code Regeln eins Datensatzes in der Konstruktion
 */
public class iPartsRelatedInfoLongCodeRuleData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoLongCodeRuleData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_LONG_CODE_RULE_DATA, iPartsConst.RELATED_INFO_LONG_CODE_RULE_DATA_TEXT,
              false, true, EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                                      RelatedInfoDisplayOption.DEFVISIBLE,
                                      RelatedInfoDisplayOption.COMMONENTRY,
                                      RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoLongCodeRuleDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        // Die RelatedInfo gibt es nur an einem Stücklisteneintrag in der Konstruktion -> Prüfen, ob es sich um einen
        // Stücklisteneintrag handelt
        PartListEntryId partListEntryId = dataConnector.getRelatedInfoData().getAsPartListEntryId();
        if (partListEntryId != null) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), partListEntryId.getOwnerAssemblyId());
            if (iPartsRelatedInfoLongCodeRuleDataForm.relatedInfoIsVisible(assembly)) {
                return RelatedInfoDisplayState.ENABLED;
            }
        }
        return RelatedInfoDisplayState.HIDE;
    }
}
