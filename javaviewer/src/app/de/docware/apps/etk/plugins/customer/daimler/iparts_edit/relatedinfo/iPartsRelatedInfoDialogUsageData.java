/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;

import java.util.EnumSet;

/**
 * RelatedInfo für den Verwendungsnachweis von DIALOG-Konstruktions-Stücklisten im AS
 */
public class iPartsRelatedInfoDialogUsageData extends EtkRelatedInfoBaseImpl {

    public iPartsRelatedInfoDialogUsageData() {
        super(iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_USAGE_DATA, iPartsConst.RELATED_INFO_DIALOG_USAGE_TEXT, false, true,
              EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                         RelatedInfoDisplayOption.DEFVISIBLE,
                         RelatedInfoDisplayOption.COMMONENTRY,
                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        return new iPartsRelatedInfoDialogUsageDataForm(dataConnector, parentForm, this);
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            return RelatedInfoDisplayState.HIDE;
        } else {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(dataConnector.getProject(), dataConnector.getRelatedInfoData().getAsPartListEntryId().getOwnerAssemblyId());
            if (!iPartsRelatedInfoDialogUsageDataForm.relatedInfoIsVisible(assembly)) {
                return RelatedInfoDisplayState.HIDE;
            }
            EtkDataPartListEntry partListEntry = dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject());
            boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
            try {
                partListEntry.setLogLoadFieldIfNeeded(false); // Das Feld DIALOG_DD_RETAIL_USE ist beim Aufruf aus der Suche noch nicht geladen
                if (!partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(iPartsDataVirtualFieldsDefinition.RETAIL_ASSIGNED)) {
                    return RelatedInfoDisplayState.HIDE;
                }
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
        return RelatedInfoDisplayState.ENABLED;
    }
}
